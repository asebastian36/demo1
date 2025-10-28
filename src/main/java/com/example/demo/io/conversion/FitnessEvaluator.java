package com.example.demo.io.conversion;

import com.example.demo.function.ChromosomeBasedFitnessFunction;
import com.example.demo.function.FitnessFunction;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FitnessEvaluator {

    private final Map<String, FitnessFunction> fitnessFunctions;

    public FitnessEvaluator(Map<String, FitnessFunction> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public double toAdaptiveSingle(double x, String functionType) {
        FitnessFunction function = fitnessFunctions.get(functionType);
        if (function == null) {
            throw new IllegalArgumentException("Función desconocida: " + functionType);
        }

        // Si la función es basada en cromosoma, devolver 0.0
        if (function instanceof ChromosomeBasedFitnessFunction) {
            return 0.0; // O lanzar una excepción más específica si prefieres
        }

        return function.evaluate(x);
    }

    public FitnessFunction getFunction(String functionType) {
        return fitnessFunctions.get(functionType);
    }
}