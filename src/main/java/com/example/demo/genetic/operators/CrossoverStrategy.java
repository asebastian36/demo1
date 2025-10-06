package com.example.demo.genetic.operators;

public interface CrossoverStrategy {
    String[] crossover(String parent1, String parent2);
}