package com.example.demo.genetic.algorithm;

// package com.example.demo.genetic.algorithm;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ExecutionStatus {
    private final ConcurrentHashMap<String, AtomicInteger> currentGeneration = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> totalGenerations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<String>> status = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> completed = new ConcurrentHashMap<>();

    public void startExecution(String sessionId, int totalGens) {
        currentGeneration.put(sessionId, new AtomicInteger(0));
        totalGenerations.put(sessionId, new AtomicInteger(totalGens));
        status.put(sessionId, new AtomicReference<>("Iniciando..."));
        completed.put(sessionId, false);
    }

    public void updateGeneration(String sessionId, int gen) {
        AtomicInteger current = currentGeneration.get(sessionId);
        if (current != null) {
            current.set(gen);
        }
    }

    public void markCompleted(String sessionId) {
        completed.put(sessionId, true);
    }

    public boolean isCompleted(String sessionId) {
        return completed.getOrDefault(sessionId, true);
    }

    public int getCurrentGeneration(String sessionId) {
        AtomicInteger current = currentGeneration.get(sessionId);
        return current != null ? current.get() : 0;
    }

    public int getTotalGenerations(String sessionId) {
        AtomicInteger total = totalGenerations.get(sessionId);
        return total != null ? total.get() : 0;
    }
}