package com.example.demo.service.crossover;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class SinglePointCrossoverStrategy implements CrossoverStrategy {

    private final Random random = new Random();
    private ThreadLocal<Integer> forcedPoint = new ThreadLocal<>();

    public void setForcedPoint(int point) {
        forcedPoint.set(point);
    }

    public void clearForcedPoint() {
        forcedPoint.remove();
    }

    @Override
    public String[] crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }
        int L = parent1.length();

        int point = forcedPoint.get() != null ? forcedPoint.get() : 1 + random.nextInt(L - 1);

        String child1 = parent1.substring(0, point) + parent2.substring(point);
        String child2 = parent2.substring(0, point) + parent1.substring(point);

        return new String[]{child1, child2};
    }
}