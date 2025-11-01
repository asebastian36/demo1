package com.example.demo.execution.strategy.selection;

import com.example.demo.execution.model.Individual;
import com.example.demo.genetic.operators.SelectionStrategy;
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
            // CORRECCIÓN: Si el fitness total es <= 0, buscamos el mejor individuo (el más cercano a 0)
            // y lo seleccionamos para darle una oportunidad al AG de escapar del 0.

            // 1. Encontrar el individuo con el fitness más alto (menos negativo o más cercano a cero)
            Individual bestFitInZeroPop = population.stream()
                    .max(Comparator.comparingDouble(Individual::getAdaptative))
                    .orElse(population.getFirst()); // Fallback al primero

            log.warn("⚠️ Fitness total <= 0. Forzando selección del individuo con mejor fitness ({}).", bestFitInZeroPop.getAdaptative());
            return bestFitInZeroPop;
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