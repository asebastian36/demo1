package com.example.demo.execution.strategy.evaluation;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.FitnessFunction;
import com.example.demo.function.credit.CreditRiskFitnessFunction;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.strategy.FitnessEvaluationStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreditRiskFitnessStrategy implements FitnessEvaluationStrategy {

    private final BinaryToDecimalConverter binaryConverter;

    public CreditRiskFitnessStrategy(BinaryToDecimalConverter binaryConverter) {
        this.binaryConverter = binaryConverter;
    }

    @Override
    public List<Individual> evaluatePopulation(List<String> binaries, double xmin, double xmax, int L, int generation, FitnessFunction function) {
        if (!(function instanceof CreditRiskFitnessFunction)) {
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