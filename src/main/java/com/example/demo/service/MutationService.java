package com.example.demo.service;

import com.example.demo.entities.Individual;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class MutationService {

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
        return individuals.stream()
                .map(indiv -> mutate(indiv, mutationRate, L))
                .collect(Collectors.toList());
    }
}