package com.example.demo.service.crossover;

import org.springframework.stereotype.Component;

@Component
public class SinglePointCrossoverStrategy implements CrossoverStrategy {

    @Override
    public String[] crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }
        int point = 4; // después del bit 4 (0-based: índices 0-3 y 4-end)
        String child1 = parent2.substring(0, point) + parent1.substring(point);
        String child2 = parent1.substring(0, point) + parent2.substring(point);
        return new String[]{child1, child2};
    }
}