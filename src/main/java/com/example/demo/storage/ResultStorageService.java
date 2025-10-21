package com.example.demo.storage;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ResultStorageService {

    private static class ResultData {
        final Object data;
        final long timestamp;

        ResultData(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, ResultData> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public ResultStorageService() {
        cleaner.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    public void store(String sessionId, Object data) {
        storage.put(sessionId, new ResultData(data, System.currentTimeMillis()));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String sessionId, Class<T> type) {
        ResultData data = storage.get(sessionId);
        return data != null && type.isInstance(data.data) ? (T) data.data : null;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        storage.entrySet().removeIf(entry -> (now - entry.getValue().timestamp) > 300_000); // 5 minutos
    }
}