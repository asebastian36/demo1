package com.example.demo.function;

import com.example.demo.io.conversion.BinaryToDecimalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractSegmentedFitnessFunction implements ChromosomeBasedFitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(AbstractSegmentedFitnessFunction.class);

    @Override
    public Map<String, Object> decodeAndInterpret(String binary, BinaryToDecimalConverter converter) {
        int[] segmentLengths = getSegmentLengths();
        String[] variableNames = getVariableNames();
        double[][] ranges = getRanges();

        if (binary == null || binary.length() != getTotalLength()) {
            log.error("Binario de longitud incorrecta: {}", binary != null ? binary.length() : 0);
            return Collections.emptyMap();
        }

        Map<String, Object> interpretation = new LinkedHashMap<>();
        int currentPosition = 0;
        double totalFitness = 0.0;

        for (int i = 0; i < segmentLengths.length; i++) {
            int L = segmentLengths[i];
            String segment = binary.substring(currentPosition, currentPosition + L);
            currentPosition += L;

            double xmin = ranges[i][0];
            double xmax = ranges[i][1];

            long decimalValue = converter.convertBinaryToInt(segment);
            double realValue = xmin + decimalValue * (xmax - xmin) / (Math.pow(2, L) - 1);

            String formattedValue = String.format("%.2f", realValue);
            interpretation.put(variableNames[i], formattedValue);
        }

        // Calcular fitness total usando la lógica específica
        double fitness = evaluate(binary);
        interpretation.put("Satisfacción Total", String.format("%.4f", fitness));
        interpretation.put("Interpretación", getInterpretation(fitness));

        return interpretation;
    }

    protected abstract double[][] getRanges();
    protected abstract String getInterpretation(double fitness);
    protected abstract int getTotalLength();

    // Método por defecto para funciones que no usan x real
    @Override
    public double evaluate(double x) {
        throw new UnsupportedOperationException("Esta función opera directamente sobre el cromosoma binario.");
    }
}