package com.example.demo.storage;

import com.example.demo.execution.elitist.model.CombinationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ElitistResultCache {

    private static class ResultData {
        final List<CombinationResult> results;
        final long timestamp;

        ResultData(List<CombinationResult> results, long timestamp) {
            this.results = results;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, ResultData> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public ElitistResultCache() {
        cleaner.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    public void store(String sessionId, List<CombinationResult> results) {
        storage.put(sessionId, new ResultData(results, System.currentTimeMillis()));
    }

    @SuppressWarnings("unchecked")
    public List<CombinationResult> get(String sessionId) {
        ResultData data = storage.get(sessionId);
        return data != null ? data.results : null;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        storage.entrySet().removeIf(entry -> (now - entry.getValue().timestamp) > 900_000); // 15 minutos
    }
}