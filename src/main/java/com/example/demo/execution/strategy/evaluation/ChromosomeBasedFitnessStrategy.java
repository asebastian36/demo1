package com.example.demo.execution.strategy.evaluation;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.ChromosomeBasedFitnessFunction;
import com.example.demo.function.FitnessFunction;
import com.example.demo.strategy.FitnessEvaluationStrategy;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChromosomeBasedFitnessStrategy implements FitnessEvaluationStrategy {

    @Override
    public List<Individual> evaluatePopulation(List<String> binaries, double xmin, double xmax,
                                               int L, int generation, FitnessFunction function) {
        if (!(function instanceof ChromosomeBasedFitnessFunction chromosomeFunction)) {
            throw new IllegalArgumentException("Función no compatible");
        }

        return binaries.stream()
                .map(binary -> {
                    double fitness = chromosomeFunction.evaluate(binary);
                    return new Individual(binary, 0.0, fitness, generation);
                })
                .sorted(Comparator.comparingDouble(Individual::getAdaptative).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean supports(String functionType) {
        return "credit".equals(functionType) || "consumo".equals(functionType);
    }

    @Override
    public boolean checkConvergence(List<Individual> generation, FitnessFunction function,
                                    double convergenceThreshold) {
        // Lógica de convergencia para funciones basadas en cromosoma
        double targetX = function.getTargetX();
        long countConverged = generation.stream()
                .filter(ind -> ind.getAdaptative() >= targetX * convergenceThreshold)
                .count();
        return (double) countConverged / generation.size() >= convergenceThreshold;
    }
}
