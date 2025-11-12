// src/main/java/com/example/demo/function/netflix/NetflixRecommendationFitnessFunction.java
package com.example.demo.function.netflix;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("netflix")
public class NetflixRecommendationFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(NetflixRecommendationFitnessFunction.class);

    // Datos base de los 6 títulos [Duración, Valoración (R), Afinidad (A)] [cite: 71]
    private static final double[][] TITLE_DATA = {
            {60.0, 9.0, 9.0},  // 1: Stranger Things
            {50.0, 8.0, 6.0},  // 2: The Crown
            {45.0, 8.0, 7.0},  // 3: Lupin
            {45.0, 7.0, 8.0},  // 4: You
            {60.0, 9.0, 9.0},  // 5: The Witcher
            {40.0, 8.0, 10.0}  // 6: Wednesday
    };

    private static final String[] VARIABLE_NAMES = {
            "Stranger Things", "The Crown", "Lupin", "You", "The Witcher", "Wednesday"
    };

    private static final int MAX_DURATION_MINUTES = 720; // 12 horas [cite: 83]
    private static final double ALPHA = 0.05; // Penalización por exceso de duración [cite: 95]
    private static final int TOTAL_LENGTH = 6; // Codificación Binaria (6 bits)

    @Override
    public double evaluate(String binary) {
        if (binary == null || binary.length() != TOTAL_LENGTH) {
            log.error("Cromosoma inválido para Netflix: longitud={}", binary != null ? binary.length() : 0);
            return 0.0;
        }

        double totalDuration = 0.0; // D [cite: 94]
        double sumAfinityRating = 0.0; // Σ(A_i + R_i) [cite: 88, 90, 92]
        int selectedTitlesCount = 0; // n [cite: 93]

        // 1. Decodificación y suma de métricas
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            if (binary.charAt(i) == '1') { // Si el título se incluye [cite: 74, 80]
                totalDuration += TITLE_DATA[i][0]; // Duración (D)
                sumAfinityRating += TITLE_DATA[i][1] + TITLE_DATA[i][2]; // Suma de R_i + A_i
                selectedTitlesCount++;
            }
        }

        if (selectedTitlesCount == 0) {
            return 0.0; // No hay títulos, fitness 0
        }

        // 2. Cálculo de Satisfacción (S) [cite: 88]
        // S = Σ(A_i + R_i) / n
        double satisfactionAverage = sumAfinityRating / selectedTitlesCount;

        // 3. Cálculo de Penalización por Exceso de Duración [cite: 88, 95]
        // Penalización = α * max(0, D - 720)
        double durationPenalty = ALPHA * Math.max(0, totalDuration - MAX_DURATION_MINUTES);

        // 4. Fitness Final (F): F = S - Penalización [cite: 97]
        double fitness = satisfactionAverage - durationPenalty;

        // El fitness debe ser maximizado.
        return Math.max(fitness, 0.0);
    }

    // --- Métodos de Interpretación y Configuración (Para ChromosomeBasedFitnessFunction) ---

    @Override
    public Map<String, Object> decodeAndInterpret(String binary, com.example.demo.io.conversion.BinaryToDecimalConverter converter) {

        Map<String, Object> interpretation = new LinkedHashMap<>();
        StringBuilder recommendedTitles = new StringBuilder();

        double totalDuration = 0.0;
        double sumAfinityRating = 0.0;
        int selectedTitlesCount = 0;

        // Decodificación y métricas
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            if (binary.charAt(i) == '1') {
                if (selectedTitlesCount > 0) recommendedTitles.append(", ");
                recommendedTitles.append(VARIABLE_NAMES[i]);

                totalDuration += TITLE_DATA[i][0];
                sumAfinityRating += TITLE_DATA[i][1] + TITLE_DATA[i][2];
                selectedTitlesCount++;
            }
        }

        double satisfactionAverage = selectedTitlesCount > 0 ? sumAfinityRating / selectedTitlesCount : 0.0;
        double durationPenalty = ALPHA * Math.max(0, totalDuration - MAX_DURATION_MINUTES);
        double fitness = satisfactionAverage - durationPenalty;

        interpretation.put("Títulos", recommendedTitles.toString().isEmpty() ? "Ninguno" : recommendedTitles.toString());
        interpretation.put("Duración Total", String.format("%.0f min", totalDuration));
        interpretation.put("Penalización", String.format("%.2f", durationPenalty));
        interpretation.put("Satisfacción (S)", String.format("%.4f", satisfactionAverage));
        interpretation.put("Fitness (F)", String.format("%.4f", fitness));
        interpretation.put("Interpretación", getInterpretation(fitness));

        return interpretation;
    }

    @Override
    public int[] getSegmentLengths() {
        return new int[]{TOTAL_LENGTH};
    }

    @Override
    public String[] getVariableNames() {
        return VARIABLE_NAMES;
    }

    @Override
    protected double[][] getRanges() {
        return Collections.emptyList().toArray(new double[0][0]);
    }

    @Override
    protected String getInterpretation(double fitness) {
        // Ajustado para el valor máximo de 18.0
        if (fitness >= 17.5) return "Excelente (Máxima Satisfacción)";
        else if (fitness >= 16.0) return "Muy buena";
        else if (fitness >= 14.0) return "Buena";
        else return "Aceptable / Revisar";
    }

    @Override
    protected int getTotalLength() {
        return TOTAL_LENGTH; // 6 bits
    }

    @Override
    public String getName() {
        return "Recomendación Netflix";
    }

    @Override
    public double getOptimalValue() {
        return 18;
    }

    @Override
    public double getTargetX() {
        return getOptimalValue();
    }
}