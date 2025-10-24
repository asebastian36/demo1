package com.example.demo.function.credit;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("credit")
public class CreditRiskFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(CreditRiskFitnessFunction.class);

    private static final int[] SEGMENT_LENGTHS = {8, 6, 6, 7, 7};
    private static final String[] VARIABLE_NAMES = {"Ingreso", "Edad", "Historial Crediticio", "Deuda", "Ahorro"};
    private static final double[][] RANGES = {
            {5000.0, 100000.0},
            {18.0, 70.0},
            {0.0, 1.0},
            {0.0, 100.0},
            {0.0, 100.0}
    };
    private static final double[] WEIGHTS = {0.30, 0.10, 0.25, 0.20, 0.15};

    @Override
    public double evaluate(String binary) {
        if (binary == null || binary.length() != 34) {
            return 0.0;
        }

        int currentPosition = 0;
        double totalFitness = 0.0;

        for (int i = 0; i < SEGMENT_LENGTHS.length; i++) {
            int L = SEGMENT_LENGTHS[i];
            String segment = binary.substring(currentPosition, currentPosition + L);
            currentPosition += L;

            double xmin = RANGES[i][0];
            double xmax = RANGES[i][1];
            double weight = WEIGHTS[i];

            long decimalValue = Long.parseLong(segment, 2);
            double realValue = xmin + decimalValue * (xmax - xmin) / (Math.pow(2, L) - 1);
            double fi = (realValue - xmin) / (xmax - xmin);
            if (Double.isNaN(fi) || Double.isInfinite(fi)) fi = 0.0;

            if ("Historial Crediticio".equals(VARIABLE_NAMES[i])) {
                fi = realValue;
            }

            double contribution_fi = fi;
            if ("Deuda".equals(VARIABLE_NAMES[i])) {
                contribution_fi = 1.0 - fi;
            }

            totalFitness += contribution_fi * weight;
        }

        return Math.min(Math.max(totalFitness, 0.0), 1.0);
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
        if (fitness >= 0.81) return "Riesgo MUY BAJO (Ideal)";
        else if (fitness >= 0.61) return "Riesgo BAJO (Confiable)";
        else if (fitness >= 0.31) return "Riesgo MEDIO (Aceptable)";
        else return "Riesgo ALTO (Rechazado)";
    }

    @Override
    protected int getTotalLength() {
        return 34;
    }

    @Override
    public String getName() {
        return "Crédito: f(x) = 0.3fᵢ + ... + 0.15fₐ";
    }

    @Override
    public double getTargetX() {
        return 1.0;
    }

    @Override
    public double getOptimalValue() {
        return 1.0;
    }
}