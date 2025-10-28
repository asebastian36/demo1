package com.example.demo.execution.elitist.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ElitistExecutionContext {
    private final String sessionId;
    private final int totalCombinations;
    private final AtomicInteger completedCombinations = new AtomicInteger(0);
    private final ConcurrentHashMap<String, String> errors = new ConcurrentHashMap<>();

    public ElitistExecutionContext(String sessionId, int totalCombinations) {
        this.sessionId = sessionId;
        this.totalCombinations = totalCombinations;
    }

    public void incrementCompleted() {
        completedCombinations.incrementAndGet();
    }

    public void addError(String combinationId, String error) {
        errors.put(combinationId, error);
    }

    public String getSessionId() { return sessionId; }
    public int getTotalCombinations() { return totalCombinations; }
    public int getCompletedCombinations() { return completedCombinations.get(); }
    public boolean isCompleted() { return completedCombinations.get() >= totalCombinations; }
    public ConcurrentHashMap<String, String> getErrors() { return errors; }
}