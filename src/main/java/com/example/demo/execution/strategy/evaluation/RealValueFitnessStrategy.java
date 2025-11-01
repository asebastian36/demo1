package com.example.demo.execution.strategy.evaluation;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.FitnessFunction;
import com.example.demo.io.conversion.DecimalToRealConverter;
import com.example.demo.strategy.FitnessEvaluationStrategy;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RealValueFitnessStrategy implements FitnessEvaluationStrategy {

    private final com.example.demo.io.conversion.BinaryToDecimalConverter binaryConverterService;
    private final DecimalToRealConverter realConverterService;

    public RealValueFitnessStrategy(com.example.demo.io.conversion.BinaryToDecimalConverter binaryConverterService,
                                    DecimalToRealConverter realConverterService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
    }

    @Override
    public List<Individual> evaluatePopulation(List<String> binaries, double xmin, double xmax, int L, int generation, FitnessFunction function) {
        List<Long> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
        List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
        List<Double> fitnessValues = reals.stream()
                .map(function::evaluate)
                .toList();

        return IntStream.range(0, binaries.size())
                .mapToObj(i -> new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generation))
                .sorted(Comparator.comparingDouble(Individual::getAdaptative).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean supports(String functionType) {
        return !"credit".equals(functionType) && !"consumo".equals(functionType) && !"farmacologia".equals(functionType);
    }

    @Override
    public boolean checkConvergence(List<Individual> generation, FitnessFunction function, double convergenceThreshold) {
        double targetX = function.getTargetX();
        long countConverged = generation.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - targetX) < 0.1)
                .count();
        double percentage = (double) countConverged / generation.size();
        return percentage >= convergenceThreshold;
    }
}