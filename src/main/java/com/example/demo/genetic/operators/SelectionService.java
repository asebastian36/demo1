package com.example.demo.genetic.operators;

import com.example.demo.entities.Individual;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SelectionService {

    private final Map<String, SelectionStrategy> selectionStrategies;

    public SelectionService(Map<String, SelectionStrategy> selectionStrategies) {
        this.selectionStrategies = selectionStrategies;
    }

    public List<Individual[]> selectPairs(
            String selectionType,
            List<Individual> population,
            int numPairs) {

        SelectionStrategy strategy = selectionStrategies.get(selectionType);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de selección desconocido: " + selectionType);
        }
        return strategy.selectPairs(population, numPairs);
    }
}
