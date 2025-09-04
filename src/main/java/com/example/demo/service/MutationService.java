package com.example.demo.service;

import com.example.demo.entities.Individual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MutationService {

    private static final Logger log = LoggerFactory.getLogger(MutationService.class);
    private final Random random = new Random();

    public Individual mutate(Individual individual, double mutationRate, int L) {
        if (random.nextDouble() >= mutationRate) {
            return individual;
        }

        String binary = individual.getBinary();
        int index = random.nextInt(binary.length());
        char bit = binary.charAt(index);
        char newBit = (bit == '0') ? '1' : '0';

        String mutated = new StringBuilder(binary)
                .replace(index, index + 1, String.valueOf(newBit))
                .toString();

        // Asegurar longitud L
        if (mutated.length() > L) {
            mutated = mutated.substring(mutated.length() - L);
        } else if (mutated.length() < L) {
            mutated = "0".repeat(L - mutated.length()) + mutated;
        }

        return new Individual(mutated, individual.getReal(), individual.getAdaptative(), individual.getGeneration());
    }

    public List<Individual> mutateAll(List<Individual> individuals, double mutationRate, int L) {
        log.info("Iniciando mutación masiva: {} individuos, tasa={}%", individuals.size(), mutationRate * 100);

        List<Individual> result = new ArrayList<>();
        int mutationsApplied = 0;

        for (Individual individual : individuals) {
            Individual mutated = mutate(individual, mutationRate, L);
            if (!individual.getBinary().equals(mutated.getBinary())) {
                mutationsApplied++;
            }
            result.add(mutated);
        }

        log.info("Mutación finalizada: {} de {} individuos mutados", mutationsApplied, individuals.size());
        return result;
    }
}