package com.example.demo.genetic.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.*;
import com.example.demo.genetic.function.FitnessFunction;
import com.example.demo.genetic.operators.*;
import com.example.demo.genetic.metrics.MetricsService;
import com.example.demo.genetic.population.PopulationSource;
import com.example.demo.strategy.FitnessEvaluationStrategy;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmService {

    private static final Logger log = LoggerFactory.getLogger(GeneticAlgorithmService.class);

    private final AdaptiveFunctionService adaptiveFunctionService;
    private final CrossoverService crossoverService;
    private final MutationService mutationService;
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final MetricsService metricsService;
    private final Map<String, SelectionStrategy> selectionStrategies;
    private final Map<String, PopulationSource> populationSources;
    private final List<FitnessEvaluationStrategy> evaluationStrategies;

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   MetricsService metricsService,
                                   Map<String, SelectionStrategy> selectionStrategies,
                                   Map<String, PopulationSource> populationSources,
                                   List<FitnessEvaluationStrategy> evaluationStrategies) {
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.metricsService = metricsService;
        this.selectionStrategies = selectionStrategies;
        this.populationSources = populationSources;
        this.evaluationStrategies = evaluationStrategies;
    }

    public List<List<Individual>> runEvolution(
            List<String> fileBinaries,
            double xmin,
            double xmax,
            int L,
            String functionType,
            String selectionType,
            String crossoverType,
            String mutationType,
            int populationSize,
            int maxGenerations,
            double mutationRatePerBit,
            double crossoverRate,
            String populationSourceType) {

        return runEvolutionWithStatus(fileBinaries, xmin, xmax, L, functionType, selectionType,
                crossoverType, mutationType, populationSize, maxGenerations, mutationRatePerBit,
                crossoverRate, populationSourceType, "default", null, 0.8);
    }

    public List<List<Individual>> runEvolutionWithStatus(
            List<String> fileBinaries,
            double xmin,
            double xmax,
            int L,
            String functionType,
            String selectionType,
            String crossoverType,
            String mutationType,
            int populationSize,
            int maxGenerations,
            double mutationRatePerBit,
            double crossoverRate,
            String populationSourceType,
            String sessionId,
            ExecutionContext context,
            double convergenceThreshold) {

        Instant start = Instant.now();
        int finalL = L;

        log.info("🚀 Iniciando algoritmo genético - Función: {}, Generaciones: {}, Población estimada: {}",
                adaptiveFunctionService.getFunction(functionType).getName(),
                maxGenerations,
                "file".equals(populationSourceType) ? (fileBinaries != null ? fileBinaries.size() : 0) : populationSize);

        PopulationSource populationSource = populationSources.get(populationSourceType);
        if (populationSource == null) {
            throw new IllegalArgumentException("Fuente de población desconocida: " + populationSourceType);
        }

        if ("file".equals(populationSourceType)) {
            if (fileBinaries == null || fileBinaries.isEmpty()) {
                throw new IllegalArgumentException("No se proporcionaron binarios para el modo archivo");
            }
            ((com.example.demo.genetic.population.FilePopulationSource) populationSource).setBinaries(fileBinaries);
        } else if ("random".equals(populationSourceType)) {
            ((com.example.demo.genetic.population.RandomPopulationSource) populationSource).setPopulationSize(populationSize);
        }

        List<String> currentBinaries = populationSource.generatePopulation(finalL);
        log.info("→ Población inicial generada ({}): {} individuos (L={})",
                populationSource.getName(), currentBinaries.size(), finalL);

        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();
        boolean convergenceAchieved = false;
        int actualGenerations = 0;

        FitnessEvaluationStrategy strategy = evaluationStrategies.stream()
                .filter(s -> s.supports(functionType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estrategia no encontrada para: " + functionType));

        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        if (function == null) {
            throw new IllegalArgumentException("Función desconocida: " + functionType);
        }

        for (int gen = 0; gen < maxGenerations; gen++) {
            actualGenerations = gen + 1;

            if (context != null) {
                context.updateGeneration(actualGenerations);
            }

            // 🚨 Log esencial: cada 100 generaciones o en la última
            if (gen % 100 == 0 || gen == maxGenerations - 1) {
                log.info("Generación {} de {}", actualGenerations, maxGenerations);
            }

            List<Individual> generation = strategy.evaluatePopulation(
                    currentBinaries, xmin, xmax, finalL, gen, function
            );
            generations.add(generation);

            if (strategy.checkConvergence(generation, function, convergenceThreshold)) {
                log.info("✅ Convergencia del {}% alcanzada en generación {}",
                        (int)(convergenceThreshold * 100), actualGenerations);
                convergenceAchieved = true;
                if (context != null) {
                    context.updateGeneration(actualGenerations);
                }
                break;
            }

            if (gen < maxGenerations - 1) {
                int currentPopulationSize = currentBinaries.size();
                int numPairs = (currentPopulationSize + 1) / 2;

                SelectionStrategy selection = selectionStrategies.get(selectionType);
                if (selection == null) {
                    throw new IllegalArgumentException("Tipo de selección desconocido: " + selectionType);
                }

                if ("tournament".equals(selectionType) && selection instanceof com.example.demo.genetic.operators.TournamentSelection) {
                    ((com.example.demo.genetic.operators.TournamentSelection) selection).configure(xmin, xmax, finalL, functionType);
                }

                List<Individual[]> parentPairs = selection.selectPairs(generation, numPairs);

                List<Individual> offspring = new ArrayList<>();
                int crossoverCount = 0;

                for (int i = 0; i < parentPairs.size(); i++) {
                    Individual[] pair = parentPairs.get(i);
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), finalL);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), finalL);

                    CrossoverResult result;
                    if (Math.random() < crossoverRate) {
                        result = crossoverService.crossoverWithLogging(
                                bin1, bin2, crossoverType, i + 1, finalL, xmin, xmax, functionType);
                        crossoverCount++;
                    } else {
                        result = new CrossoverResult(new String[]{bin1, bin2});
                    }

                    String[] children = result.getChildren();

                    for (String childBinary : children) {
                        double adaptative = 0.0;
                        double real = 0.0;

                        if ("credit".equals(functionType)) {
                            FitnessFunction func = adaptiveFunctionService.getFunction(functionType);
                            adaptative = func.evaluate(childBinary);
                            real = 0.0;
                        } else {
                            long decimal = binaryConverterService.convertBinaryToInt(childBinary);
                            real = realConverterService.toRealSingle(decimal, xmin, xmax, finalL);
                            adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
                        }

                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }

                mutationService.applyToGenerationWithLogging(offspring, mutationRatePerBit, finalL, gen + 1, mutationType, functionType);

                if (offspring.size() > currentPopulationSize) {
                    offspring = new ArrayList<>(offspring.subList(0, currentPopulationSize));
                }

                currentBinaries = offspring.stream().map(Individual::getBinary).collect(Collectors.toList());
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        log.info("✅ Algoritmo finalizado - Tiempo: {} min {} seg, Generaciones: {}, Convergencia: {}",
                minutes, seconds, actualGenerations, convergenceAchieved ? "sí" : "no");

        FitnessFunction func = adaptiveFunctionService.getFunction(functionType);
        double optimalValue = func.getOptimalValue();

        int generation90Percent = metricsService.findGeneration90Percent(generations, optimalValue);
        double avgDiversity = metricsService.calculateAverageDiversity(generations);
        double threshold90 = optimalValue * 0.9;

        metricsService.logComparisonMetrics(generation90Percent, actualGenerations, threshold90, optimalValue, avgDiversity);
        metricsService.logConvergenceResults(generations.getLast(), func);

        if (context != null) {
            context.markCompleted();
        }

        return generations;
    }
}