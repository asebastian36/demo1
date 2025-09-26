package com.example.demo.service.function;

public interface FitnessFunction {
    double evaluate(double x);
    String getName();
    double getOptimalValue(); // Para verificar convergencia
    double getTargetX(); // Valor de x donde está el óptimo (para convergencia)
}
