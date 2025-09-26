package com.example.demo.service.function;

import org.springframework.stereotype.Component;

@Component("function2")
public class Function2 implements FitnessFunction {

    @Override
    public double evaluate(double x) {
        return x * x + 2 * x + 5;
    }

    @Override
    public String getName() {
        return "Función 2: f(x) = x² + 2x + 5";
    }

    @Override
    public double getOptimalValue() {
        return 173.0; // f(12) = 144 + 24 + 5 = 173
    }

    @Override
    public double getTargetX() {
        return 12.0; // Convergencia en x = 12 (máximo en el rango [2,12])
    }
}