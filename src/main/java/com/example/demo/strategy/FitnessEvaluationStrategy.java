package com.example.demo.strategy;

import com.example.demo.entities.Individual;
import com.example.demo.genetic.function.FitnessFunction;

import java.util.List;

public interface FitnessEvaluationStrategy {
    List<Individual> evaluatePopulation(List<String> binaries, double xmin, double xmax, int L, int generation, FitnessFunction function);
    boolean supports(String functionType);
    boolean checkConvergence(List<Individual> generation, FitnessFunction function, double convergenceThreshold);
}