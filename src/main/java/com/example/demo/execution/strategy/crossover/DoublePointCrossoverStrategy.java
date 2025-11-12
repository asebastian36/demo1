// src/main/java/com/example/demo/execution/strategy/crossover/DoublePointCrossoverStrategy.java
package com.example.demo.execution.strategy.crossover;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component("double")
public class DoublePointCrossoverStrategy implements CrossoverStrategy {

    private final Random random = new Random();

    @Override
    public CrossoverResult crossover(String parent1, String parent2) {
        if (parent1.length() != parent2.length()) {
            throw new IllegalArgumentException("Longitudes diferentes: " + parent1 + ", " + parent2);
        }
        int L = parent1.length();

        // Si la longitud es muy pequeña, evitamos el cruce doble o usamos puntos triviales
        if (L < 2) {
            // No se puede cruzar. Regresa padres como hijos.
            return new CrossoverResult(new String[]{parent1, parent2});
        }

        // 1. Generar dos puntos de corte válidos (start y end)
        // Punto 1: entre 1 y L-2
        int point1 = 1 + random.nextInt(L - 2);

        // Punto 2: entre point1 + 1 y L-1
        int point2 = point1 + 1 + random.nextInt(L - point1 - 1);

        // Asegurar que point1 < point2
        int start = Math.min(point1, point2);
        int end = Math.max(point1, point2);

        // La condición (start < end) y (end < L) siempre se cumplen con la lógica de random

        String child1 = parent1.substring(0, start) +
                parent2.substring(start, end) +
                parent1.substring(end);

        String child2 = parent2.substring(0, start) +
                parent1.substring(start, end) +
                parent2.substring(end);

        return new CrossoverResult(new String[]{child1, child2}, start, end);
    }
}