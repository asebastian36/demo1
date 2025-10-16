package com.example.demo.genetic.function;

import org.springframework.stereotype.Component;

@Component("function2")
public class Function2 implements FitnessFunction {

    @Override
    public double evaluate(double x) {
        return x * x + 2 * x + 5;
    }

    // 🚨 IMPLEMENTACIÓN DEL MÉTODO default (OPCIONAL, heredará la excepción)
    // @Override
    // public double evaluate(String binary) {
    //     throw new UnsupportedOperationException("Función 2 opera sobre un valor real (double), no sobre el binario.");
    // }

    @Override
    public String getName() {
        return "Función 2: f(x) = x² + 2x + 5";
    }

    @Override
    public double getOptimalValue() {
        return 173.0;
    }

    @Override
    public double getTargetX() {
        return 12.0;
    }
}