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
        for (int i = 0; i < populationSize; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < L; j++) {
                sb.append(random.nextBoolean() ? '1' : '0');
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
