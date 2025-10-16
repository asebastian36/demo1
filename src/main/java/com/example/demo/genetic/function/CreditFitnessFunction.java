package com.example.demo.genetic.function;

import com.example.demo.conversion.BinaryConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("credit")
public class CreditFitnessFunction implements FitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(CreditFitnessFunction.class);

    // Basado en el contexto: 8+6+6+7+7 = 34 bits
    private static final int[] SEGMENT_LENGTHS = {8, 6, 6, 7, 7};
    private static final String[] VARIABLE_NAMES = {"Ingreso", "Edad", "Historial Crediticio", "Deuda", "Ahorro"};
    private static final String[] UNITS = {" $", " años", " (0-1)", " %", " %"};

    // Rangos de las variables para la decodificación
    private static final double[][] RANGES = {
            {5000.0, 100000.0}, // Ingreso
            {18.0, 70.0},        // Edad
            {0.0, 1.0},          // Historial Crediticio
            {0.0, 100.0},        // Deuda
            {0.0, 100.0}         // Ahorro
    };

    // Pesos W_i de la función de aptitud
    private static final double[] WEIGHTS = {0.30, 0.10, 0.25, 0.20, 0.15};

    private final BinaryConverterService binaryConverterService;

    public CreditFitnessFunction(BinaryConverterService binaryConverterService) {
        this.binaryConverterService = binaryConverterService;
    }

    @Override
    public double evaluate(double x) {
        throw new UnsupportedOperationException("La función de Crédito no opera sobre un único valor real 'x'.");
    }

    @Override
    public double evaluate(String binary) {
        Map<String, Object> details = getInterpretationDetails(binary, binaryConverterService);
        String fitnessStr = (String) details.get("Fitness Total");
        return Double.parseDouble(fitnessStr);
    }

    /**
     * Calcula el fitness, devuelve la decodificación simple para la vista, y LOGGEA LOS DETALLES.
     * Devuelve Map<String, Object> con: {Variable: String (Valor Decodificado), 'Fitness Total': String, 'Interpretación': String}
     */
    public static Map<String, Object> getInterpretationDetails(String binary, BinaryConverterService converterService) {
        if (binary == null || binary.length() != 34) {
            log.error("Binario de longitud incorrecta: {}", binary != null ? binary.length() : 0);
            return Collections.emptyMap();
        }

        Map<String, Object> simpleInterpretation = new LinkedHashMap<>();
        int currentPosition = 0;
        double totalFitness = 0.0;
        StringBuilder logDetails = new StringBuilder("\n--- Análisis Detallado del Cromosoma ---\n");
        logDetails.append("Binario Completo: ").append(binary).append("\n");

        for (int i = 0; i < SEGMENT_LENGTHS.length; i++) {
            int L = SEGMENT_LENGTHS[i];
            String segment = binary.substring(currentPosition, currentPosition + L);
            currentPosition += L;

            double xmin = RANGES[i][0];
            double xmax = RANGES[i][1];
            double weight = WEIGHTS[i];

            // Decodificación
            long decimalValue = converterService.convertBinaryToInt(segment);
            double realValue = xmin + decimalValue * (xmax - xmin) / (Math.pow(2, L) - 1);
            double fi = (realValue - xmin) / (xmax - xmin);
            if (Double.isNaN(fi) || Double.isInfinite(fi)) fi = 0.0; // Evitar NaN/Infinity

            // Ajuste para Historial: fi = valor real
            if (VARIABLE_NAMES[i].equals("Historial Crediticio")) fi = realValue;

            // Deuda se invierte
            double contribution_fi = fi;
            if (VARIABLE_NAMES[i].equals("Deuda")) {
                contribution_fi = 1.0 - fi;
            }

            double contribution = contribution_fi * weight;
            totalFitness += contribution;

            String formattedRealValue;
            if (VARIABLE_NAMES[i].equals("Ingreso")) {
                formattedRealValue = String.format("%,.0f", realValue);
            } else {
                formattedRealValue = String.format("%.2f", realValue);
            }

            // 🚨 GUARDAR SOLO VALOR DECODIFICADO PARA LA VISTA
            simpleInterpretation.put(VARIABLE_NAMES[i], formattedRealValue + UNITS[i]);

            // 🚨 LOGUEAR DETALLES EXTENSOS
            logDetails.append(String.format("  [%s]: %s (fᵢ=%.4f) | Contribución: %.4f\n",
                    VARIABLE_NAMES[i], simpleInterpretation.get(VARIABLE_NAMES[i]), fi, contribution));
        }

        // Interpretación Final
        String overallInterpretation;
        if (totalFitness >= 0.81) overallInterpretation = "Riesgo MUY BAJO (Ideal)";
        else if (totalFitness >= 0.61) overallInterpretation = "Riesgo BAJO (Confiable)";
        else if (totalFitness >= 0.31) overallInterpretation = "Riesgo MEDIO (Aceptable)";
        else overallInterpretation = "Riesgo ALTO (Rechazado)";

        simpleInterpretation.put("Fitness Total", String.format("%.4f", totalFitness));
        simpleInterpretation.put("Interpretación", overallInterpretation);

        logDetails.append(String.format("Fitness Total: %.4f → %s\n", totalFitness, overallInterpretation));
        log.info(logDetails.toString());

        return simpleInterpretation;
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