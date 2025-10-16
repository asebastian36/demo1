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
    // La constante CONVERGENCE_THRESHOLD se ha eliminado y ahora se usa el parámetro convergenceThreshold

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

        // Llamada simplificada usando el valor por defecto de 0.8
        return runEvolutionWithStatus(fileBinaries, xmin, xmax, L, functionType, selectionType,
                crossoverType, mutationType, populationSize, maxGenerations, mutationRatePerBit,
                crossoverRate, populationSourceType, "default", null, 0.8);
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
            ExecutionStatus executionStatus,
            double convergenceThreshold) { // Parámetro de convergencia

        Instant start = Instant.now();
        int finalL = L;

        // FORZAR L=34 para la función de crédito
        if ("credit".equals(functionType)) {
            finalL = 34;
            log.info("   -> ADVERTENCIA: Función de Crédito seleccionada. L forzado a {}", finalL);
        }

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
        // Usar {} para la convergencia
        log.info("   Condición de paro: ≥{}% de convergencia o {} generaciones",
                (int)(convergenceThreshold * 100), maxGenerations);

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

        for (int gen = 0; gen < maxGenerations; gen++) {
            actualGenerations = gen + 1;

            if (executionStatus != null) {
                executionStatus.updateGeneration(sessionId, actualGenerations);
            }

            log.info(" ");
            log.info("════════════════════════════════════════════════");
            log.info("        🎯 GENERACIÓN {} de {}", actualGenerations, maxGenerations);
            log.info("════════════════════════════════════════════════");

            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, finalL, gen, functionType);
            generations.add(generation);

            // Pasar el umbral de convergencia
            if (checkConvergence(generation, functionType, convergenceThreshold)) {
                log.info("🎉 ✅ ¡CONVERGENCIA DEL {}% ALCANZADA EN GENERACIÓN {}!",
                        (int)(convergenceThreshold * 100), actualGenerations);

                convergenceAchieved = true;

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
                    ((TournamentSelection) selection).configure(xmin, xmax, finalL, functionType);
                }

                log.info("→ SELECCIÓN: {}", selection.getName());
                List<Individual[]> parentPairs = selection.selectPairs(generation, numPairs);

                // CORRECCIÓN: Usar {} y formatear el double a string antes de loguear si se requiere precisión
                log.info("→ CRUCE: Generando hijos con cruce de un punto (probabilidad = {}%)",
                        String.format("%.1f", crossoverRate * 100));
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
                            FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
                            // Se asume que el método evaluate para CreditFitnessFunction ha sido corregido a la sobrecarga de String
                            adaptative = function.evaluate(childBinary);
                            real = 0.0;
                        } else {
                            long decimal = binaryConverterService.convertBinaryToInt(childBinary);
                            real = realConverterService.toRealSingle(decimal, xmin, xmax, finalL);
                            adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
                        }

                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }
                // CORRECCIÓN: Usar {} y formatear el double a string
                log.info("→ ✅ Cruce completado: {} parejas cruzaron ({}%)", crossoverCount,
                        String.format("%.1f", (double) crossoverCount / parentPairs.size() * 100));

                // CORRECCIÓN: Usar {} y formatear el double a string
                log.info("→ MUTACIÓN ({}): Aplicando con tasa = {}%", mutationType,
                        String.format("%.3f", mutationRatePerBit * 100));

                mutationService.applyToGenerationWithLogging(offspring, mutationRatePerBit, finalL, gen + 1, mutationType, functionType);

                if (offspring.size() > currentPopulationSize) {
                    offspring = new ArrayList<>(offspring.subList(0, currentPopulationSize));
                } else if (offspring.size() < currentPopulationSize) {
                    Individual best = offspring.isEmpty() ? generation.get(0) : offspring.get(0);
                    while (offspring.size() < currentPopulationSize) {
                        offspring.add(new Individual(best.getBinary(), best.getReal(), best.getAdaptative(), gen + 1));
                    }
                }

                currentBinaries = offspring.stream().map(Individual::getBinary).collect(Collectors.toList());
                // CORRECCIÓN: Usar {}
                log.info("→ Población ajustada a {} individuos", currentBinaries.size());
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds(); // Calcular segundos restantes

        log.info(" ");
        log.info("✅✅✅ ALGORITMO FINALIZADO ✅✅✅");

        if (convergenceAchieved) {
            log.info("🏁 Detenido por convergencia en generación {}", actualGenerations);
        } else {
            log.info("🏁 Detenido por límite de generaciones ({})", maxGenerations);
        }

        // CORRECCIÓN PRINCIPAL: Usar {} para minutos y segundos
        log.info("⏱️  Tiempo total de ejecución: {} minutos {} segundos", minutes, seconds);

        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        double optimalValue = function.getOptimalValue();

        int generation90Percent = metricsService.findGeneration90Percent(generations, optimalValue);
        double avgDiversity = metricsService.calculateAverageDiversity(generations);
        double threshold90 = optimalValue * 0.9;

        metricsService.logComparisonMetrics(generation90Percent, actualGenerations, threshold90, optimalValue, avgDiversity);
        // Nota: Si usas Java 21+, getLast() es correcto. Si usas una versión anterior (ej. Java 17), usa get(generations.size() - 1)
        metricsService.logConvergenceResults(generations.get(generations.size() - 1), function);

        return generations;
    }

    // MODIFICAR FIRMA para aceptar convergenceThreshold
    private boolean checkConvergence(List<Individual> generation, String functionType, double convergenceThreshold) {
        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        double targetX = function.getTargetX();

        if ("credit".equals(functionType)) {
            long countConverged = generation.stream()
                    .filter(ind -> ind.getAdaptative() >= targetX * convergenceThreshold)
                    .count();

            double percentage = (double) countConverged / generation.size();
            boolean converged = percentage >= convergenceThreshold;

            if (converged) {
                log.info("   → ✅ Convergencia verificada: {}% (≥{}%)",
                        String.format("%.1f", percentage * 100),
                        (int)(convergenceThreshold * 100));
            } else {
                log.info("   → ⏳ Convergencia actual: {}% (<{}%)",
                        String.format("%.1f", percentage * 100),
                        (int)(convergenceThreshold * 100));
            }
            return converged;

        } else {
            // Lógica existente para funciones f(x)
            long countConverged = generation.stream()
                    .filter(ind -> Math.abs(Math.abs(ind.getReal()) - targetX) < 0.1)
                    .count();

            double percentage = (double) countConverged / generation.size();
            boolean converged = percentage >= convergenceThreshold;

            if (converged) {
                log.info("   → ✅ Convergencia verificada: {}% (≥{}%)",
                        String.format("%.1f", percentage * 100),
                        (int)(convergenceThreshold * 100));
            } else {
                log.info("   → ⏳ Convergencia actual: {}% (<{}%)",
                        String.format("%.1f", percentage * 100),
                        (int)(convergenceThreshold * 100));
            }
            return converged;
        }
    }

    private List<Individual> createOrderedGeneration(List<String> binaries, double xmin, double xmax, int L, int generationIndex, String functionType) {
        List<Individual> individuals = new ArrayList<>();
        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);

        if ("credit".equals(functionType)) {
            for (String binary : binaries) {
                double fitnessValue = function.evaluate(binary);
                individuals.add(new Individual(binary, 0.0, fitnessValue, generationIndex));
            }
        } else {
            List<Long> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
            List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
            List<Double> fitnessValues = adaptiveFunctionService.toAdaptive(reals, functionType);

            for (int i = 0; i < binaries.size(); i++) {
                individuals.add(new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generationIndex));
            }
        }

        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());
        return individuals;
    }
}