package com.example.demo.genetic.operators;

public interface CrossoverStrategy {
    CrossoverResult crossover(String parent1, String parent2);
}