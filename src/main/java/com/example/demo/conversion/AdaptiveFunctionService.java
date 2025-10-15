package com.example.demo.conversion;

import com.example.demo.genetic.function.FitnessFunction;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveFunctionService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveFunctionService.class);
    private final Map<String, FitnessFunction> fitnessFunctions;

    public AdaptiveFunctionService(Map<String, FitnessFunction> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public List<Double> toAdaptive(List<Double> realValues, String functionType) {
        FitnessFunction function = fitnessFunctions.get(functionType);
        if (function == null) {
            throw new IllegalArgumentException("Función desconocida: " + functionType);
        }

        log.debug("Aplicando función adaptativa: {} a {} valores", function.getName(), realValues.size());
        return realValues.stream()
                .map(function::evaluate)
                .collect(Collectors.toList());
    }

    public double toAdaptiveSingle(double x, String functionType) {
        FitnessFunction function = fitnessFunctions.get(functionType);
        if (function == null) {
            throw new IllegalArgumentException("Función desconocida: " + functionType);
        }
        return function.evaluate(x);
    }

    // Método para obtener la función (útil para convergencia)
    public FitnessFunction getFunction(String functionType) {
        return fitnessFunctions.get(functionType);
    }
}