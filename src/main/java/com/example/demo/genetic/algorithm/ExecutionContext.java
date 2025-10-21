package com.example.demo.genetic.algorithm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutionContext {
    private final AtomicInteger currentGeneration = new AtomicInteger(0);
    private final int totalGenerations;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public ExecutionContext(int totalGenerations) {
        this.totalGenerations = totalGenerations;
    }

    public void updateGeneration(int gen) {
        currentGeneration.set(gen);
    }

    public void markCompleted() {
        completed.set(true);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public int getCurrentGeneration() {
        return currentGeneration.get();
    }

    public int getTotalGenerations() {
        return totalGenerations;
    }
}