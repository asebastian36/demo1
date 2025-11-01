// src/main/java/com/example/demo/function/farmacia/PharmaceuticalFitnessFunction.java
package com.example.demo.function.farmacia;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.springframework.stereotype.Component;

@Component("farmacologia")
public class PharmaceuticalFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final int[] SEGMENT_LENGTHS = {10, 10, 10};
    private static final String[] VARIABLE_NAMES = {"Medicamento A", "Medicamento B", "Medicamento C"};
    private static final double[][] RANGES = {
            {0.0, 10.0}, // A
            {0.0, 10.0}, // B
            {0.0, 10.0}  // C
    };

    // Constantes de la función
    private static final double ALPHA = 0.05;
    private static final double BETA = 0.1;

    @Override
    public double evaluate(String binary) {
        if (binary == null || binary.length() != 30) {
            return 0.0;
        }

        // Decodificar los tres segmentos
        double A = decodeSegment(binary.substring(0, 10), 0.0, 10.0);
        double B = decodeSegment(binary.substring(10, 20), 0.0, 10.0);
        double C = decodeSegment(binary.substring(20, 30), 0.0, 10.0);

        // Calcular componentes
        double eficacia = 100.0 * Math.exp(-((A - 4.0) * (A - 4.0) +
                (B - 5.0) * (B - 5.0) +
                (C - 3.0) * (C - 3.0)));
        double toxicidad = 0.5 * A * A + 0.3 * B * B + 0.4 * C * C;
        double costo = 2.0 * A + 3.0 * B + 1.5 * C;

        // Función de fitness final
        double fitness = eficacia - ALPHA * toxicidad - BETA * costo;
        return Math.max(fitness, 0.0); // Evitar negativos
    }

    private double decodeSegment(String segment, double xmin, double xmax) {
        long decimal = Long.parseLong(segment, 2);
        return xmin + decimal * (xmax - xmin) / (Math.pow(2, 10) - 1);
    }

    @Override
    public int[] getSegmentLengths() {
        return SEGMENT_LENGTHS;
    }

    @Override
    public String[] getVariableNames() {
        return VARIABLE_NAMES;
    }

    @Override
    protected double[][] getRanges() {
        return RANGES;
    }

    @Override
    protected String getInterpretation(double fitness) {
        if (fitness >= 90.0) return "Combinación ÓPTIMA";
        else if (fitness >= 70.0) return "Combinación BUENA";
        else if (fitness >= 50.0) return "Combinación ACEPTABLE";
        else return "Combinación INEFICAZ";
    }

    @Override
    protected int getTotalLength() {
        return 30;
    }

    @Override
    public String getName() {
        return "Optimización Farmacológica: F = E - 0.05T - 0.1Costo";
    }

    @Override
    public double getTargetX() {
        return 100.0; // Valor máximo teórico de eficacia
    }

    @Override
    public double getOptimalValue() {
        return 100.0;
    }
}