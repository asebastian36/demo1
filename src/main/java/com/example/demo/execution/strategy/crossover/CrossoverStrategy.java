package com.example.demo.execution.strategy.crossover;

public interface CrossoverStrategy {
    CrossoverResult crossover(String parent1, String parent2);
}