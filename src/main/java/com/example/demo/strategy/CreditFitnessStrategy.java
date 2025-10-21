package com.example.demo.strategy;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.BinaryConverterService;
import com.example.demo.genetic.function.CreditFitnessFunction;
import com.example.demo.genetic.function.FitnessFunction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreditFitnessStrategy implements FitnessEvaluationStrategy {

    private final BinaryConverterService binaryConverterService;

    public CreditFitnessStrategy(BinaryConverterService binaryConverterService) {
        this.binaryConverterService = binaryConverterService;
    }

    @Override
    public List<Individual> evaluatePopulation(List<String> binaries, double xmin, double xmax, int L, int generation, FitnessFunction function) {
        if (!(function instanceof CreditFitnessFunction)) {
            throw new IllegalArgumentException("Función no compatible con estrategia de crédito");
        }
        return binaries.stream()
                .map(binary -> {
                    double fitness = function.evaluate(binary);
                    return new Individual(binary, 0.0, fitness, generation);
                })
                .sorted(Comparator.comparingDouble(Individual::getAdaptative).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean supports(String functionType) {
        return "credit".equals(functionType);
    }

    @Override
    public boolean checkConvergence(List<Individual> generation, FitnessFunction function, double convergenceThreshold) {
        double targetX = function.getTargetX();
        long countConverged = generation.stream()
                .filter(ind -> ind.getAdaptative() >= targetX * convergenceThreshold)
                .count();
        double percentage = (double) countConverged / generation.size();
        return percentage >= convergenceThreshold;
    }
}