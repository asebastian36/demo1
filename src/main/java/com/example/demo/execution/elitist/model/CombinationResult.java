package com.example.demo.execution.elitist.model;

public record CombinationResult(
        ExecutionCombination combination,
        String chartImage,
        int generationsToConverge,
        double bestFitness,
        long executionTimeMs,
        String errorMessage
) {}