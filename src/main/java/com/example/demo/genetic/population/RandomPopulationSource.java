package com.example.demo.genetic.population;

import org.springframework.stereotype.Component;
import java.util.*;

@Component("random")
public class RandomPopulationSource implements PopulationSource {

    private int populationSize;
    private final Random random = new Random();

    public RandomPopulationSource() {}

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    @Override
    public List<String> generatePopulation(int L) {
        List<String> population = new ArrayList<>();

        // 🚨 SOLUCIÓN 2: Bucle para generar L bits
        for (int i = 0; i < populationSize; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < L; j++) {
                sb.append(random.nextBoolean() ? '1' : '0');
            }
            if (sb.length() != L) {
                // Esto no debería suceder. Si pasa, hay un error mayor en la JVM/Random.
                throw new IllegalStateException("Error al generar binario: Longitud es " + sb.length() + " pero debería ser " + L);
            }
            population.add(sb.toString());
        }
        return population;
    }

    @Override
    public String getName() {
        return "Aleatoria";
    }
}