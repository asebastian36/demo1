package com.example.demo.genetic.operators;

import org.springframework.stereotype.Component;

@Component("double")
public class DoublePointCrossoverStrategy implements CrossoverStrategy {

    @Override
    public CrossoverResult crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }
        // Cruce entre bit 3 y bit 9 → índices 3 a 8 (0-based), 6 bits
        int start = 3;
        int end = 9; // exclusive end en substring

        String child1 = parent1.substring(0, start) +
                parent2.substring(start, end) +
                parent1.substring(end);

        String child2 = parent2.substring(0, start) +
                parent1.substring(start, end) +
                parent2.substring(end);

        return new CrossoverResult(new String[]{child1, child2}, start, end);
    }
}