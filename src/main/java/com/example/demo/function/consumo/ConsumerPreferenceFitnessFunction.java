package com.example.demo.function.consumo;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("consumo")
public class ConsumerPreferenceFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(ConsumerPreferenceFitnessFunction.class);
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
            log.error("Cromosoma inválido para consumo: '{}' (longitud={})", binary, binary != null ? binary.length() : "null");
            return 0.0;
        }

        double P = decodeSegment(binary.substring(0, 10));
        double C = decodeSegment(binary.substring(10, 20));
        double A = decodeSegment(binary.substring(20, 30));

        double satisfaction = (10 - P) + 0.5 * C + 0.1 * A - 0.05 * Math.pow(P - 5, 2);
        double finalSatisfaction = Math.max(satisfaction, 0.0);

        // LOG DE DIAGNÓSTICO
        log.debug("Decodificación - P={}, C={}, A={}, S={}", P, C, A, finalSatisfaction);

        return finalSatisfaction;
    }

    private double decodeSegment(String segment) {
        long decimal = Long.parseLong(segment, 2);
        return 0.0 + decimal * (10.0 - 0.0) / (Math.pow(2, 10) - 1);
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
        // Ajustado para el nuevo óptimo de 14.75
        if (fitness >= 14.0) return "Satisfacción MUY ALTA";
        else if (fitness >= 12.0) return "Satisfacción ALTA";
        else if (fitness >= 8.0) return "Satisfacción MEDIA";
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
        return 14.75; // Valor Máximo Teórico
    }

    @Override
    public double getOptimalValue() {
        return 14.75; // Valor Máximo Teórico
    }
}