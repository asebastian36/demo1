package com.example.demo.metrics;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.FitnessFunction;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlgorithmMetricsService {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmMetricsService.class);

    /**
     * Calcula en qué generación se alcanza el 90% del valor óptimo.
     *
     * @param generations lista de generaciones
     * @param optimalValue valor óptimo de la función
     * @return número de generación (1-indexed) o -1 si no se alcanza
     */
    public int findGeneration90Percent(List<List<Individual>> generations, double optimalValue) {
        double threshold90 = optimalValue * 0.9;

        for (int gen = 0; gen < generations.size(); gen++) {
            List<Individual> generation = generations.get(gen);
            if (!generation.isEmpty()) {
                // Se usa getFirst() que requiere Java 21+ o se asume que generation[0] es el mejor,
                // lo cual es cierto por el ordenamiento en GeneticAlgorithmService.
                double bestFitness = generation.getFirst().getAdaptative();
                if (bestFitness >= threshold90) {
                    return gen + 1; // 1-indexed
                }
            }
        }
        return -1;
    }

    /**
     * Calcula la diversidad genética promedio de todas las generaciones.
     *
     * @param generations lista de generaciones
     * @return diversidad genética promedio (0.0 a 0.5)
     */
    public double calculateAverageDiversity(List<List<Individual>> generations) {
        if (generations == null || generations.isEmpty()) {
            return 0.0;
        }

        double totalDiversity = 0.0;
        int validGenerations = 0;

        for (List<Individual> generation : generations) {
            if (generation != null && !generation.isEmpty()) {
                double diversity = calculateGeneticDiversity(generation);
                totalDiversity += diversity;
                validGenerations++;
            }
        }

        return validGenerations > 0 ? totalDiversity / validGenerations : 0.0;
    }

    /**
     * Calcula la diversidad genética de una generación específica.
     *
     * @param generation lista de individuos
     * @return diversidad genética (0.0 a 0.5)
     */
    public double calculateGeneticDiversity(List<Individual> generation) {
        if (generation == null || generation.isEmpty() || generation.getFirst().getBinary() == null) {
            return 0.0;
        }

        int L = generation.getFirst().getBinary().length();
        int populationSize = generation.size();

        if (populationSize <= 1) {
            return 0.0;
        }

        double totalDiversity = 0.0;

        // Para cada posición de bit
        for (int bitPos = 0; bitPos < L; bitPos++) {
            int onesCount = 0;

            // Contar cuántos individuos tienen '1' en esta posición
            for (Individual individual : generation) {
                if (individual.getBinary().charAt(bitPos) == '1') {
                    onesCount++;
                }
            }

            double p = (double) onesCount / populationSize; // Proporción de 1s
            double diversityAtPosition = 2.0 * p * (1.0 - p); // Máximo = 0.5 cuando p=0.5
            totalDiversity += diversityAtPosition;
        }

        return totalDiversity / L; // Promedio por posición
    }

    /**
     * Verifica la convergencia final y muestra logs.
     *
     * @param finalGeneration última generación
     * @param function función objetivo
     */
    public void logConvergenceResults(List<Individual> finalGeneration, FitnessFunction function) {
        double targetX = function.getTargetX();

        long countConverged = finalGeneration.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - targetX) < 0.1)
                .count();

        double percentage = (double) countConverged / finalGeneration.size() * 100;

        // Logs corregidos usando {} y String.format para el formato decimal
        log.info("");
        log.info("📊 RESULTADO FINAL DE CONVERGENCIA:");
        log.info("   → Individuos en x ≈ ±{}: {} de {}", String.format("%.1f", targetX), countConverged, finalGeneration.size());
        log.info("   → Porcentaje: {}%", String.format("%.2f", percentage));

        if (percentage >= 80) {
            log.info("🎉 ✅ ¡CONVERGENCIA EXITOSA! (≥80%% en x ≈ ±{})", String.format("%.1f", targetX));
        } else {
            log.warn("⚠️ ❌ Convergencia insuficiente (<80%% en x ≈ ±{})", String.format("%.1f", targetX));
        }
    }

    /**
     * Muestra las métricas de comparación en los logs.
     *
     * @param generation90Percent generación donde se alcanza el 90%
     * @param numGenerations total de generaciones ejecutadas
     * @param threshold90 umbral del 90%
     * @param optimalValue valor óptimo
     * @param avgDiversity diversidad genética promedio
     */
    public void logComparisonMetrics(int generation90Percent, int numGenerations,
                                     double threshold90, double optimalValue, double avgDiversity) {

        // Logs corregidos usando {} y String.format para el formato decimal
        if (generation90Percent != -1) {
            log.info("📊 MÉTRICA DE COMPARACIÓN:");
            log.info("   → Convergencia al 90%% del óptimo en generación: {}", generation90Percent);
            log.info("   → Umbral del 90%%: {} (óptimo: {})", String.format("%.2f", threshold90), String.format("%.2f", optimalValue));
        } else {
            log.info("📊 MÉTRICA DE COMPARACIÓN:");
            log.info("   → No se alcanzó el 90%% del óptimo en {} generaciones", numGenerations);
        }

        if (avgDiversity > 0) {
            log.info("🧬 DIVERSIDAD GENÉTICA PROMEDIO: {}", String.format("%.4f", avgDiversity));
            log.info("   → Rango: 0.0 (mínima) a 0.5 (máxima)");
        }
    }
}