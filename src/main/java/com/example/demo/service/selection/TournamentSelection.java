package com.example.demo.service.selection;

import com.example.demo.entities.Individual;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("tournament")
public class TournamentSelection implements SelectionStrategy {

    private final Random random = new Random();

    @Override
    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        List<Individual[]> pairs = new ArrayList<>();
        int tournamentSize = Math.min(3, population.size()); // Tamaño del torneo

        for (int i = 0; i < numPairs; i++) {
            Individual parent1 = selectTournament(population, tournamentSize);
            Individual parent2 = selectTournament(population, tournamentSize);
            pairs.add(new Individual[]{parent1, parent2});
        }

        return pairs;
    }

    private Individual selectTournament(List<Individual> population, int tournamentSize) {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }

        // Seleccionar el mejor del torneo
        return tournament.stream()
                .max((i1, i2) -> Double.compare(i1.getAdaptative(), i2.getAdaptative()))
                .orElse(population.get(0));
    }

    @Override
    public String getName() {
        return "Selección por Torneo";
    }
}