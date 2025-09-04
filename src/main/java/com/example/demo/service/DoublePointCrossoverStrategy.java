package com.example.demo.service;

import org.springframework.stereotype.Component;

@Component
public class DoublePointCrossoverStrategy implements CrossoverStrategy {

    @Override
    public String[] crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }
        // Cruce entre bit 3 y bit 9 → índices 2 a 8 (0-based), 7 bits
        int start = 3;
        int end = 9; // exclusive end en substring

        String child1 = parent1.substring(0, start) +
                parent2.substring(start, end) +
                parent1.substring(end);

        String child2 = parent2.substring(0, start) +
                parent1.substring(start, end) +
                parent2.substring(end);

        return new String[]{child1, child2};
    }
}