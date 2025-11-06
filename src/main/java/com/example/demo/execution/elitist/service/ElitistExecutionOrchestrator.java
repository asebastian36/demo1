package com.example.demo.execution.elitist.service;

import com.example.demo.execution.elitist.model.*;
import com.example.demo.execution.model.Individual;
import com.example.demo.execution.service.GeneticAlgorithmCore;
import com.example.demo.execution.strategy.crossover.CrossoverStrategy;
import com.example.demo.execution.strategy.mutation.MutationStrategy;
import com.example.demo.genetic.operators.SelectionStrategy;
import com.example.demo.function.FitnessFunction;
import com.example.demo.io.conversion.FitnessEvaluator;
import com.example.demo.metrics.AlgorithmMetricsService;
import com.example.demo.visualization.FitnessChartGenerator;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ElitistExecutionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ElitistExecutionOrchestrator.class);

    private final GeneticAlgorithmCore algorithmCore;
    private final FitnessChartGenerator chartGenerator;
    private final AlgorithmMetricsService metricsService;
    private final FitnessEvaluator fitnessEvaluator;
    private final Map<String, SelectionStrategy> selectionStrategies;
    private final Map<String, CrossoverStrategy> crossoverStrategies;
    private final Map<String, MutationStrategy> mutationStrategies;
    private final ElitistExecutionStatusService statusService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ElitistExecutionOrchestrator(
            GeneticAlgorithmCore algorithmCore,
            FitnessChartGenerator chartGenerator,
            AlgorithmMetricsService metricsService,
            FitnessEvaluator fitnessEvaluator,
            Map<String, SelectionStrategy> selectionStrategies,
            Map<String, CrossoverStrategy> crossoverStrategies,
            Map<String, MutationStrategy> mutationStrategies,
            ElitistExecutionStatusService statusService) {
        this.algorithmCore = algorithmCore;
        this.chartGenerator = chartGenerator;
        this.metricsService = metricsService;
        this.fitnessEvaluator = fitnessEvaluator;
        this.selectionStrategies = selectionStrategies;
        this.crossoverStrategies = crossoverStrategies;
        this.mutationStrategies = mutationStrategies;
        this.statusService = statusService;
    }

    public List<CombinationResult> executeAllCombinations(
            String sessionId,
            List<String> binaries,
            double xmin,
            double xmax,
            int L,
            String functionType,
            String populationSourceType,
            int populationSize,
            int maxGenerations,
            double mutationRate,
            double crossoverRate,
            double convergenceThreshold) {

        List<ExecutionCombination> combinations = generateCombinations();
        statusService.startExecution(sessionId, combinations.size());

        List<CompletableFuture<CombinationResult>> futures = new ArrayList<>();
        for (ExecutionCombination combo : combinations) {
            CompletableFuture<CombinationResult> future = CompletableFuture
                    .supplyAsync(() -> executeCombination(
                            sessionId,
                            combo.toString(),
                            binaries, xmin, xmax, L, functionType, populationSourceType,
                            populationSize, maxGenerations, mutationRate, crossoverRate,
                            convergenceThreshold, combo
                    ), executor);
            futures.add(future);
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private List<ExecutionCombination> generateCombinations() {
        List<ExecutionCombination> combinations = new ArrayList<>();
        for (String selection : selectionStrategies.keySet()) {
            for (String crossover : crossoverStrategies.keySet()) {
                for (String mutation : mutationStrategies.keySet()) {
                    combinations.add(new ExecutionCombination(selection, crossover, mutation));
                }
            }
        }
        return combinations;
    }

    private CombinationResult executeCombination(
            String sessionId,
            String combinationId,
            List<String> binaries,
            double xmin,
            double xmax,
            int L,
            String functionType,
            String populationSourceType,
            int populationSize,
            int maxGenerations,
            double mutationRate,
            double crossoverRate,
            double convergenceThreshold,
            ExecutionCombination combo) {

        long start = System.currentTimeMillis();
        try {
            var generations = algorithmCore.runEvolutionWithStatus(
                    binaries,
                    xmin,
                    xmax,
                    L,
                    functionType,
                    combo.selectionType(),
                    combo.crossoverType(),
                    combo.mutationType(),
                    populationSize,
                    maxGenerations,
                    mutationRate,
                    crossoverRate,
                    populationSourceType,
                    "elitist-" + combinationId,
                    null,
                    convergenceThreshold
            );

            log.info("Combinación {} - Generaciones: {}", combinationId, generations.size());
            if (!generations.isEmpty()) {
                log.info("Combinación {} - Generación 1 - Individuos: {}", combinationId, generations.getFirst().size());
                if (!generations.getFirst().isEmpty()) {
                    log.info("Combinación {} - Primer fitness: {}", combinationId, generations.getFirst().getFirst().getAdaptative());
                }
            }

            FitnessFunction function = fitnessEvaluator.getFunction(functionType);
            double optimalValue = function.getOptimalValue();
            int generationsToConverge = metricsService.findGeneration90Percent(generations, optimalValue);
            double bestFitness = generations.isEmpty() ? 0.0 : generations.getLast().getFirst().getAdaptative();

            List<List<Double>> fitnessByGeneration = generations.stream()
                    .map(gen -> gen.stream().map(Individual::getAdaptative).collect(Collectors.toList()))
                    .collect(Collectors.toList());

            log.info("Combinación {} - fitnessByGeneration size: {}", combinationId, fitnessByGeneration.size());
            if (!fitnessByGeneration.isEmpty() && !fitnessByGeneration.getFirst().isEmpty()) {
                log.info("Combinación {} - fitnessByGeneration[0][0]: {}", combinationId, fitnessByGeneration.getFirst().getFirst());
            }

            // Generar la gráfica de convergencia (líneas)
            String chartImage = chartGenerator.generateAdaptativeChart(fitnessByGeneration, functionType);

            // Generar el nuevo gráfico de distribución (Box Plot)
            String distributionChartImage = chartGenerator.generateDistributionChart(generations, functionType);

            long time = System.currentTimeMillis() - start;
            statusService.incrementCompleted(sessionId);

            // Se asume que CombinationResult fue actualizado para recibir distributionChartImage
            return new CombinationResult(combo, chartImage, distributionChartImage, generationsToConverge, bestFitness, time, null);

        } catch (Exception e) {
            long time = System.currentTimeMillis() - start;
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            statusService.addError(sessionId, combinationId, errorMsg);
            statusService.incrementCompleted(sessionId);
            // Devolver 'error' para ambas imágenes en caso de fallo
            return new CombinationResult(combo, "error", "error", -1, Double.NEGATIVE_INFINITY, time, errorMsg);
        }
    }
}