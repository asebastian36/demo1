package com.example.demo.genetic.function;

public interface FitnessFunction {
    double evaluate(double x);

    // 🚨 NUEVO MÉTODO: Para funciones que operan directamente sobre el cromosoma (ej: Crédito)
    default double evaluate(String binary) {
        throw new UnsupportedOperationException("La función de fitness no soporta la evaluación directa del binario. Se espera un valor real 'x'.");
    }

    String getName();
    double getOptimalValue();
    double getTargetX();
}