package com.example.demo.genetic.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.*;
import com.example.demo.genetic.function.FitnessFunction;
import com.example.demo.genetic.operators.*;
import com.example.demo.genetic.metrics.MetricsService;
import com.example.demo.genetic.population.PopulationSource;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmService {

    private static final Logger log = LoggerFactory.getLogger(GeneticAlgorithmService.class);
    private static final double CONVERGENCE_THRESHOLD = 0.8; // 80% de convergencia

    private final AdaptiveFunctionService adaptiveFunctionService;
    private final CrossoverService crossoverService;
    private final MutationService mutationService;
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final Map<String, SelectionStrategy> selectionStrategies;
    private final Map<String, PopulationSource> populationSources;
    private final MetricsService metricsService;

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   MetricsService metricsService,
                                   Map<String, SelectionStrategy> selectionStrategies,
                                   Map<String, PopulationSource> populationSources) {
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.metricsService = metricsService;
        this.selectionStrategies = selectionStrategies;
        this.populationSources = populationSources;
    }

    @Transactional
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
                crossoverRate, populationSourceType, "default", null);
    }

    @Transactional
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
            ExecutionStatus executionStatus) {

        Instant start = Instant.now();

        log.info("🚀 INICIANDO ALGORITMO GENÉTICO");
        log.info("   Función: {}", adaptiveFunctionService.getFunction(functionType).getName());
        log.info("   Modo de población: {}", populationSourceType);
        log.info("   Máximo de generaciones: {}", maxGenerations);
        log.info("   Selección: {}", selectionType);
        log.info("   Cruce: {}", crossoverType);
        log.info("   Mutación: {}", mutationType);
        log.info("   Prob. Cruce: {}%", crossoverRate * 100);
        log.info("   Prob. Mutación: {}%", mutationRatePerBit * 100);
        log.info("   Rango: x ∈ [{}, {}]", xmin, xmax);
        log.info("   Condición de paro: ≥{}% de convergencia o {} generaciones",
                (int)(CONVERGENCE_THRESHOLD * 100), maxGenerations);

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

        List<String> currentBinaries = populationSource.generatePopulation(L);
        log.info("→ Población inicial generada ({}): {} individuos",
                populationSource.getName(), currentBinaries.size());

        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();
        boolean convergenceAchieved = false;
        int actualGenerations = 0;

        for (int gen = 0; gen < maxGenerations; gen++) {
            actualGenerations = gen + 1;

            // ✅ ACTUALIZAR ESTADO DE EJECUCIÓN
            if (executionStatus != null) {
                executionStatus.updateGeneration(sessionId, actualGenerations);
            }

            log.info(" ");
            log.info("════════════════════════════════════════════════");
            log.info("        🎯 GENERACIÓN {} de {}", actualGenerations, maxGenerations);
            log.info("════════════════════════════════════════════════");

            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen, functionType);
            generations.add(generation);

            if (checkConvergence(generation, functionType)) {
                log.info("🎉 ✅ ¡CONVERGENCIA DEL {}% ALCANZADA EN GENERACIÓN {}!",
                        (int)(CONVERGENCE_THRESHOLD * 100), actualGenerations);
                convergenceAchieved = true;

                // ✅ ACTUALIZAR ESTADO FINAL
                if (executionStatus != null) {
                    executionStatus.updateGeneration(sessionId, actualGenerations);
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

                if ("tournament".equals(selectionType) && selection instanceof TournamentSelection) {
                    ((TournamentSelection) selection).configure(xmin, xmax, L, functionType);
                }

                log.info("→ SELECCIÓN: {}", selection.getName());
                List<Individual[]> parentPairs = selection.selectPairs(generation, numPairs);

                log.info("→ CRUCE: Generando hijos con cruce de un punto (probabilidad = %.1f%%)",
                        crossoverRate * 100);
                List<Individual> offspring = new ArrayList<>();
                int crossoverCount = 0;

                for (int i = 0; i < parentPairs.size(); i++) {
                    Individual[] pair = parentPairs.get(i);
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

                    CrossoverResult result;
                    if (Math.random() < crossoverRate) {
                        result = crossoverService.crossoverWithLogging(
                                bin1, bin2, crossoverType, i + 1, L, xmin, xmax, functionType);
                        crossoverCount++;
                    } else {
                        result = new CrossoverResult(new String[]{bin1, bin2});
                    }

                    String[] children = result.getChildren();

                    for (String childBinary : children) {
                        int decimal = binaryConverterService.convertBinaryToInt(childBinary);
                        double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                        double adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }
                log.info("→ ✅ Cruce completado: %d parejas cruzaron (%.1f%%)", crossoverCount,
                        (double) crossoverCount / parentPairs.size() * 100);

                log.info("→ MUTACIÓN (%s): Aplicando con tasa = %.3f%%", mutationType, mutationRatePerBit * 100);
                mutationService.applyToGenerationWithLogging(offspring, mutationRatePerBit, L, gen + 1, mutationType, functionType);

                if (offspring.size() > currentPopulationSize) {
                    offspring = new ArrayList<>(offspring.subList(0, currentPopulationSize));
                } else if (offspring.size() < currentPopulationSize) {
                    Individual best = offspring.isEmpty() ? generation.get(0) : offspring.get(0);
                    while (offspring.size() < currentPopulationSize) {
                        offspring.add(new Individual(best.getBinary(), best.getReal(), best.getAdaptative(), gen + 1));
                    }
                }

                currentBinaries = offspring.stream().map(Individual::getBinary).collect(Collectors.toList());
                log.info("→ Población ajustada a %d individuos", currentBinaries.size());
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info(" ");
        log.info("✅✅✅ ALGORITMO FINALIZADO ✅✅✅");

        if (convergenceAchieved) {
            log.info("🏁 Detenido por convergencia en generación {}", actualGenerations);
        } else {
            log.info("🏁 Detenido por límite de generaciones ({})", maxGenerations);
        }

        log.info("⏱️  Tiempo total de ejecución: %d minutos %d segundos",
                duration.toMinutes(), duration.minusMinutes(duration.toMinutes()).getSeconds());

        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        double optimalValue = function.getOptimalValue();

        int generation90Percent = metricsService.findGeneration90Percent(generations, optimalValue);
        double avgDiversity = metricsService.calculateAverageDiversity(generations);
        double threshold90 = optimalValue * 0.9;

        metricsService.logComparisonMetrics(generation90Percent, actualGenerations, threshold90, optimalValue, avgDiversity);
        metricsService.logConvergenceResults(generations.get(generations.size() - 1), function);

        return generations;
    }

    private boolean checkConvergence(List<Individual> generation, String functionType) {
        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        double targetX = function.getTargetX();

        long countConverged = generation.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - targetX) < 0.1)
                .count();

        double percentage = (double) countConverged / generation.size();
        boolean converged = percentage >= CONVERGENCE_THRESHOLD;

        if (converged) {
            log.info("   → ✅ Convergencia verificada: {}% (≥{}%)",
                    String.format("%.1f", percentage * 100),
                    (int)(CONVERGENCE_THRESHOLD * 100));
        } else {
            log.info("   → ⏳ Convergencia actual: {}% (<{}%)",
                    String.format("%.1f", percentage * 100),
                    (int)(CONVERGENCE_THRESHOLD * 100));
        }

        return converged;
    }

    private List<Individual> createOrderedGeneration(List<String> binaries, double xmin, double xmax, int L, int generationIndex, String functionType) {
        List<Integer> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
        List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
        List<Double> fitnessValues = adaptiveFunctionService.toAdaptive(reals, functionType);

        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < binaries.size(); i++) {
            individuals.add(new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generationIndex));
        }

        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());
        return individuals;
    }
}