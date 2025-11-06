package com.example.demo.execution.elitist.model;

public record CombinationResult(
        ExecutionCombination combination,
        String chartImage,
        String distributionChartImage, // Campo para la nueva gráfica de distribución
        int generationsToConverge,
        double bestFitness,
        long executionTimeMs,
        String errorMessage
) {}