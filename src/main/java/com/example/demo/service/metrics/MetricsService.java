package com.example.demo.service.metrics;

import com.example.demo.entities.Individual;
import com.example.demo.service.function.FitnessFunction;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

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
                double bestFitness = generation.get(0).getAdaptative();
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
        if (generation == null || generation.isEmpty() || generation.get(0).getBinary() == null) {
            return 0.0;
        }

        int L = generation.get(0).getBinary().length();
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
        log.info(" ");
        log.info("📊 RESULTADO FINAL DE CONVERGENCIA:");
        log.info("   → Individuos en x ≈ ±%.1f: %d de %d", targetX, countConverged, finalGeneration.size());
        log.info("   → Porcentaje: %.2f%%", percentage);

        if (percentage >= 80) {
            log.info("🎉 ✅ ¡CONVERGENCIA EXITOSA! (≥80%% en x ≈ ±%.1f)", targetX);
        } else {
            log.warn("⚠️ ❌ Convergencia insuficiente (<80%% en x ≈ ±%.1f)", targetX);
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
        if (generation90Percent != -1) {
            log.info("📊 MÉTRICA DE COMPARACIÓN:");
            log.info("   → Convergencia al 90%% del óptimo en generación: %d", generation90Percent);
            log.info("   → Umbral del 90%%: %.2f (óptimo: %.2f)", threshold90, optimalValue);
        } else {
            log.info("📊 MÉTRICA DE COMPARACIÓN:");
            log.info("   → No se alcanzó el 90%% del óptimo en %d generaciones", numGenerations);
        }

        if (avgDiversity > 0) {
            log.info("🧬 DIVERSIDAD GENÉTICA PROMEDIO: %.4f", avgDiversity);
            log.info("   → Rango: 0.0 (mínima) a 0.5 (máxima)");
        }
    }
}
