package com.example.demo.execution.elitist.model;

public record ExecutionCombination(
        String selectionType,
        String crossoverType,
        String mutationType
) {
    @Override
    public String toString() {
        return selectionType + " + " + crossoverType + " + " + mutationType;
    }
}