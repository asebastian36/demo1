package com.example.demo.genetic.operators;

import com.example.demo.entities.Individual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("roulette")
public class RouletteSelection implements SelectionStrategy {

    private static final Logger log = LoggerFactory.getLogger(RouletteSelection.class);
    private final Random random = new Random();

    @Override
    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        log.debug("Seleccionando {} parejas por ruleta...", numPairs);
        List<Individual[]> pairs = new ArrayList<>();

        for (int i = 0; i < numPairs; i++) {
            Individual parent1 = select(population);
            Individual parent2 = select(population);

            // Evitar que sea el mismo (opcional)
            while (parent1 == parent2 && population.size() > 1) {
                parent2 = select(population);
            }

            log.trace("Pareja {}: Padre1={} (f={}), Padre2={} (f={})",
                    i + 1,
                    parent1.getBinary(), parent1.getAdaptative(),
                    parent2.getBinary(), parent2.getAdaptative());

            pairs.add(new Individual[]{parent1, parent2});
        }

        return pairs;
    }

    /**
     * Selecciona un individuo usando ruleta de probabilidad proporcional al adaptativo.
     */
    private Individual select(List<Individual> population) {
        double totalFitness = population.stream()
                .mapToDouble(Individual::getAdaptative)
                .sum();

        if (totalFitness <= 0) {
            // Si todos son negativos o cero, seleccionar al azar
            Individual randomPick = population.get(random.nextInt(population.size()));
            log.trace("⚠️ Fitness total <= 0. Selección aleatoria: {}", randomPick.getBinary());
            return randomPick;
        }

        double rand = random.nextDouble() * totalFitness;
        double cumulative = 0.0;

        for (Individual individual : population) {
            cumulative += individual.getAdaptative();
            if (rand <= cumulative) {
                log.trace("🎯 Seleccionado por ruleta: {} (f(x)={})", individual.getBinary(), individual.getAdaptative());
                return individual;
            }
        }

        // Por seguridad, devuelve el último
        Individual last = population.getLast();
        log.warn("⚠️ Selección por defecto (último): {}", last.getBinary());
        return last;
    }

    @Override
    public String getName() {
        return "Selección por Ruleta";
    }
}