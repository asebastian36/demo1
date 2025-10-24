package com.example.demo.execution.strategy.selection;

import com.example.demo.execution.model.Individual;
import com.example.demo.genetic.operators.SelectionStrategy;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("hierarchical")
public class HierarchicalSelection implements SelectionStrategy {

    @Override
    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        // Ordenar por aptitud (descendente)
        List<Individual> sorted = population.stream()
                .sorted(Comparator.comparingDouble(Individual::getAdaptative).reversed())
                .toList();

        List<Individual[]> pairs = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numPairs; i++) {
            // Seleccionar dos individuos aleatorios de los mejores 30%
            int topCount = Math.max(1, (int) (sorted.size() * 0.3));
            Individual p1 = sorted.get(random.nextInt(topCount));
            Individual p2 = sorted.get(random.nextInt(topCount));

            pairs.add(new Individual[]{p1, p2});
        }

        return pairs;
    }

    @Override
    public String getName() {
        return "Selección Jerárquica";
    }
}