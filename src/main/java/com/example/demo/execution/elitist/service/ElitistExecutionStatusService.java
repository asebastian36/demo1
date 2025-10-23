// src/main/java/com/example/demo/execution/elitist/service/ElitistExecutionStatusService.java
package com.example.demo.execution.elitist.service;

import com.example.demo.execution.elitist.model.ElitistExecutionContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ElitistExecutionStatusService {
    private final Map<String, ElitistExecutionContext> contexts = new ConcurrentHashMap<>();

    public void startExecution(String sessionId, int totalCombinations) {
        contexts.put(sessionId, new ElitistExecutionContext(sessionId, totalCombinations));
    }

    public void incrementCompleted(String sessionId) {
        ElitistExecutionContext context = contexts.get(sessionId);
        if (context != null) {
            context.incrementCompleted();
        }
    }

    public void addError(String sessionId, String combinationId, String error) {
        ElitistExecutionContext context = contexts.get(sessionId);
        if (context != null) {
            context.addError(combinationId, error);
        }
    }

    public ElitistExecutionContext getContext(String sessionId) {
        return contexts.get(sessionId);
    }
}