package com.example.demo.service.selection;

import com.example.demo.entities.Individual;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RouletteSelectionService {

    private final Random random = new Random();

    /**
     * Selecciona un individuo usando ruleta de probabilidad proporcional al adaptativo.
     */
    public Individual select(List<Individual> population) {
        double totalFitness = population.stream()
                .mapToDouble(Individual::getAdaptative)
                .sum();

        if (totalFitness <= 0) {
            // Si todos son negativos o cero, seleccionar al azar
            return population.get(random.nextInt(population.size()));
        }

        double rand = random.nextDouble() * totalFitness;
        double cumulative = 0.0;

        for (Individual individual : population) {
            cumulative += individual.getAdaptative();
            if (rand <= cumulative) {
                return individual;
            }
        }

        // Por seguridad, devuelve el último
        return population.get(population.size() - 1);
    }

    /**
     * Selecciona pares de padres para cruces.
     */
    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        List<Individual[]> pairs = new ArrayList<>();
        for (int i = 0; i < numPairs; i++) {
            Individual parent1 = select(population);
            Individual parent2 = select(population);
            // Evitar que sea el mismo (opcional)
            while (parent1 == parent2 && population.size() > 1) {
                parent2 = select(population);
            }
            pairs.add(new Individual[]{parent1, parent2});
        }
        return pairs;
    }
}