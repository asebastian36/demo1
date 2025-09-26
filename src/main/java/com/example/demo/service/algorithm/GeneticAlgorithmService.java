package com.example.demo.service.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.service.crossover.CrossoverService;
import com.example.demo.service.conversion.AdaptiveFunctionService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.conversion.RealConverterService;
import com.example.demo.service.mutation.MutationService;
import com.example.demo.service.persistence.IndividualService;
import com.example.demo.service.selection.SelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmService {

    private static final Logger log = LoggerFactory.getLogger(GeneticAlgorithmService.class);

    private final AdaptiveFunctionService adaptiveFunctionService;
    private final CrossoverService crossoverService;
    private final MutationService mutationService;
    private final BinaryConverterService binaryConverterService;
    private final IndividualService individualService;
    private final RealConverterService realConverterService;
    private final Map<String, SelectionStrategy> selectionStrategies; // Inyectado por Spring

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   IndividualService individualService,
                                   Map<String, SelectionStrategy> selectionStrategies) {
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.individualService = individualService;
        this.selectionStrategies = selectionStrategies;
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               String selectionType,
                                               String crossoverType,
                                               String mutationType,
                                               int populationSize,
                                               int numGenerations,
                                               double mutationRatePerBit,
                                               double crossoverRate) {

        Instant start = Instant.now();

        log.info("🚀 INICIANDO ALGORITMO GENÉTICO PARA FUNCIÓN 5");
        log.info("   Función: f(x) = (x² - 1)² → Buscando MÁXIMO en x = ±3 (f=64)");
        log.info("   Población: {} individuos", populationSize);
        log.info("   Generaciones: {}", numGenerations);
        log.info("   Selección: {}", getStrategyName(selectionStrategies, selectionType));
        log.info("   Cruce: {}", crossoverType);
        log.info("   Mutación: {}", mutationType);
        log.info("   Prob. Cruce: {}%", crossoverRate * 100);
        log.info("   Prob. Mutación: {}%", mutationRatePerBit * 100);
        log.info("   Rango: x ∈ [{}, {}]", xmin, xmax);

        List<String> currentBinaries = generateInitialPopulation(initialBinaries, L, populationSize);
        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();

        for (int gen = 0; gen < numGenerations; gen++) {
            log.info(" ");
            log.info("════════════════════════════════════════════════");
            log.info("        🎯 GENERACIÓN {} de {}", gen + 1, numGenerations);
            log.info("════════════════════════════════════════════════");

            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);
            generations.add(generation);

            if (gen < numGenerations - 1) {
                // SELECCIÓN MODULAR
                SelectionStrategy selection = selectionStrategies.get(selectionType);
                if (selection == null) {
                    throw new IllegalArgumentException("Tipo de selección desconocido: " + selectionType);
                }
                log.info("→ SELECCIÓN: {}", selection.getName());
                List<Individual[]> parentPairs = selection.selectPairs(generation, populationSize / 2);

                log.info("→ CRUCE: Generando {} hijos con cruce de un punto (probabilidad = {}%)",
                        populationSize, crossoverRate * 100);
                List<Individual> offspring = new ArrayList<>();
                int crossoverCount = 0;

                for (int i = 0; i < parentPairs.size(); i++) {
                    Individual[] pair = parentPairs.get(i);
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

                    String[] children;
                    if (Math.random() < crossoverRate) {
                        children = crossoverService.crossoverWithLogging(bin1, bin2, crossoverType, i + 1, L, xmin, xmax);
                        crossoverCount++;
                    } else {
                        children = new String[]{bin1, bin2};
                    }

                    for (String childBinary : children) {
                        int decimal = binaryConverterService.convertBinaryToInt(childBinary);
                        double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                        double adaptative = adaptiveFunctionService.toAdaptiveSingle(real);
                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }
                log.info("→ ✅ Cruce completado: {} parejas cruzaron ({}%)", crossoverCount,
                        String.format("%.1f", (double) crossoverCount / parentPairs.size() * 100));

                log.info("→ MUTACIÓN ({}): Aplicando con tasa = {}%", mutationType, mutationRatePerBit * 100);
                mutationService.applyToGenerationWithLogging(offspring, mutationRatePerBit, L, gen + 1, mutationType);

                currentBinaries = offspring.stream().map(Individual::getBinary).collect(Collectors.toList());
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info(" ");
        log.info("✅✅✅ ALGORITMO FINALIZADO ✅✅✅");
        log.info("⏱️  Tiempo total de ejecución: {} minutos {} segundos",
                duration.toMinutes(), duration.minusMinutes(duration.toMinutes()).getSeconds());

        verifyConvergence(generations.get(generations.size() - 1));

        return generations;
    }

    private String getStrategyName(Map<String, SelectionStrategy> strategies, String key) {
        SelectionStrategy strategy = strategies.get(key);
        return strategy != null ? strategy.getName() : key;
    }

    private List<String> generateInitialPopulation(List<String> initialBinaries, int L, int populationSize) {
        List<String> population = new ArrayList<>();
        Random rand = new Random();

        for (String bin : initialBinaries) {
            if (population.size() < populationSize) {
                population.add(binaryConverterService.normalizeBinary(bin, L));
            }
        }

        while (population.size() < populationSize) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < L; i++) {
                sb.append(rand.nextBoolean() ? '1' : '0');
            }
            population.add(sb.toString());
        }

        log.info("→ Población inicial generada: {} individuos", population.size());
        return population;
    }

    private List<Individual> createOrderedGeneration(List<String> binaries, double xmin, double xmax, int L, int generationIndex) {
        List<Integer> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
        List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
        List<Double> fitnessValues = adaptiveFunctionService.toAdaptive(reals);

        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < binaries.size(); i++) {
            individuals.add(new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generationIndex));
        }

        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());
        return individuals;
    }

    private void verifyConvergence(List<Individual> finalGeneration) {
        long countConverged = finalGeneration.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - 3.0) < 0.1)
                .count();

        double percentage = (double) countConverged / finalGeneration.size() * 100;
        log.info(" ");
        log.info("📊 RESULTADO FINAL DE CONVERGENCIA:");
        log.info("   → Individuos en x ≈ ±3: {} de {}", countConverged, finalGeneration.size());
        log.info("   → Porcentaje: %.2f%%", percentage);

        if (percentage >= 80) {
            log.info("🎉 ✅ ¡CONVERGENCIA EXITOSA! (≥80% en x ≈ ±3)");
        } else {
            log.warn("⚠️ ❌ Convergencia insuficiente (<80% en x ≈ ±3). Considera ajustar parámetros.");
        }
    }
}