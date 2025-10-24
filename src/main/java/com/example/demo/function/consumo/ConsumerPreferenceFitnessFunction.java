package com.example.demo.function.consumo;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.springframework.stereotype.Component;

@Component("consumo")
public class ConsumerPreferenceFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final int[] SEGMENT_LENGTHS = {10, 10, 10};
    private static final String[] VARIABLE_NAMES = {"Precio", "Calidad", "Sustentabilidad"};
    private static final double[][] RANGES = {
            {0.0, 10.0}, // Precio
            {0.0, 10.0}, // Calidad  
            {0.0, 10.0}  // Sustentabilidad
    };

    @Override
    public double evaluate(String binary) {
        if (binary == null || binary.length() != 30) {
            return 0.0;
        }

        // Decodificar los tres segmentos
        String pSegment = binary.substring(0, 10);
        String cSegment = binary.substring(10, 20);
        String aSegment = binary.substring(20, 30);

        double P = decodeSegment(pSegment, 0.0, 10.0);
        double C = decodeSegment(cSegment, 0.0, 10.0);
        double A = decodeSegment(aSegment, 0.0, 10.0);

        // Fórmula de satisfacción: S = (10 - P) + 0.5*C + 0.1*A - 0.05*(P - 5)^2
        double satisfaction = (10 - P) + 0.5 * C + 0.1 * A - 0.05 * Math.pow(P - 5, 2);
        return Math.max(satisfaction, 0.0); // No negativos
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
        if (fitness >= 9.0) return "Satisfacción MUY ALTA";
        else if (fitness >= 7.0) return "Satisfacción ALTA";
        else if (fitness >= 5.0) return "Satisfacción MEDIA";
        else return "Satisfacción BAJA";
    }

    @Override
    protected int getTotalLength() {
        return 30;
    }

    @Override
    public String getName() {
        return "Preferencias de Consumo: S = (10-P) + 0.5C + 0.1A - 0.05(P-5)²";
    }

    @Override
    public double getTargetX() {
        return 10.0; // Valor máximo teórico
    }

    @Override
    public double getOptimalValue() {
        return 10.0;
    }
}