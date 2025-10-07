package com.example.demo.genetic.operators;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component("uniform")
public class UniformCrossoverStrategy implements CrossoverStrategy {

    private final Random random = new Random();

    @Override
    public CrossoverResult crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }

        int L = parent1.length();
        StringBuilder child1 = new StringBuilder(L);
        StringBuilder child2 = new StringBuilder(L);

        for (int i = 0; i < L; i++) {
            if (random.nextBoolean()) {
                child1.append(parent1.charAt(i));
                child2.append(parent2.charAt(i));
            } else {
                child1.append(parent2.charAt(i));
                child2.append(parent1.charAt(i));
            }
        }

        return new CrossoverResult(new String[]{child1.toString(), child2.toString()});
    }
}
