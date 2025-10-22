package com.example.demo.function.math;

import com.example.demo.function.FitnessFunction;
import org.springframework.stereotype.Component;

@Component("function5")
public class QuarticFunction implements FitnessFunction {

    @Override
    public double evaluate(double x) {
        return Math.pow(x * x - 1, 2);
    }

    // 🚨 IMPLEMENTACIÓN DEL MÉTODO default (OPCIONAL, heredará la excepción)

    @Override
    public String getName() {
        return "Función 5: f(x) = (x² - 1)²";
    }

    @Override
    public double getOptimalValue() {
        return 64.0;
    }

    @Override
    public double getTargetX() {
        return 3.0;
    }
}