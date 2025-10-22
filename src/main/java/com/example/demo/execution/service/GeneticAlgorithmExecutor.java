package com.example.demo.execution.service;

import com.example.demo.execution.model.AlgorithmExecutionContext;
import com.example.demo.execution.model.Individual;
import com.example.demo.storage.ExecutionResultCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmExecutor {

    private final GeneticAlgorithmCore geneticAlgorithmService;
    private final ExecutionResultCache resultStorageService;

    public GeneticAlgorithmExecutor(GeneticAlgorithmCore geneticAlgorithmService,
                                 ExecutionResultCache resultStorageService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
        this.resultStorageService = resultStorageService;
    }

    @Async
    public void executeGeneticAlgorithm(
            List<String> binaryNumbers,
            Double xmin,
            Double xmax,
            Integer L,
            String functionType,
            String selectionType,
            String crossoverType,
            String mutationType,
            Integer populationSize,
            Integer numGenerations,
            Double mutationRate,
            Double crossoverRate,
            String mode,
            String sessionId,
            AlgorithmExecutionContext context,
            Double convergenceThreshold) {

        try {
            List<List<Individual>> generations = geneticAlgorithmService.runEvolutionWithStatus(
                    binaryNumbers,
                    xmin,
                    xmax,
                    L,
                    functionType,
                    selectionType,
                    crossoverType,
                    mutationType,
                    populationSize,
                    numGenerations,
                    mutationRate,
                    crossoverRate,
                    mode,
                    sessionId,
                    context,
                    convergenceThreshold
            );

            List<List<Double>> fitnessByGeneration = generations.stream()
                    .map(gen -> gen.stream()
                            .map(Individual::getAdaptative)
                            .collect(Collectors.toList()))
                    .toList();

            resultStorageService.store(sessionId, Map.of(
                    "generations", generations,
                    "fitnessByGeneration", fitnessByGeneration,
                    "functionType", functionType,
                    "xmin", xmin,
                    "xmax", xmax,
                    "L", L
            ));

            context.markCompleted();

        } catch (Exception e) {
            context.markCompleted();
        }
    }
}