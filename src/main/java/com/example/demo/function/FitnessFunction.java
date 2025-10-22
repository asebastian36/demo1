package com.example.demo.function;

public interface FitnessFunction {
    double evaluate(double x);

    default double evaluate(String binary) {
        throw new UnsupportedOperationException("La función de fitness no soporta la evaluación directa del binario. Se espera un valor real 'x'.");
    }

    String getName();
    double getOptimalValue();
    double getTargetX();
}