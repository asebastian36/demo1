package com.example.demo.service.selection;

import com.example.demo.entities.Individual;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RouletteSelectionService {

    private static final Logger log = LoggerFactory.getLogger(RouletteSelectionService.class);
    private final Random random = new Random();

    public Individual select(List<Individual> population) {
        double totalFitness = population.stream()
                .mapToDouble(Individual::getAdaptative)
                .sum();

        if (totalFitness <= 0) {
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

        Individual last = population.get(population.size() - 1);
        log.warn("⚠️ Selección por defecto (último): {}", last.getBinary());
        return last;
    }

    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        log.debug("Seleccionando {} parejas por ruleta...", numPairs);
        List<Individual[]> pairs = new ArrayList<>();

        for (int i = 0; i < numPairs; i++) {
            Individual parent1 = select(population);
            Individual parent2 = select(population);

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
}