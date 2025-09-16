package com.example.demo.service.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.service.crossover.CrossoverService;
import com.example.demo.service.conversion.AdaptiveFunctionService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.conversion.RealConverterService;
import com.example.demo.service.mutation.MutationService;
import com.example.demo.service.persistence.IndividualService;
import com.example.demo.service.selection.RouletteSelectionService;
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
    private final RouletteSelectionService rouletteSelectionService;

    private static final int POPULATION_SIZE = 4200;
    private static final int NUM_GENERATIONS = 1000; // Dentro del rango 900-1400
    private static final double MUTATION_RATE_PER_BIT = 0.001; // 0.1% por bit
    private static final double CROSSOVER_RATE = 0.8; // 80%

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   IndividualService individualService,
                                   RouletteSelectionService rouletteSelectionService) {
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.individualService = individualService;
        this.rouletteSelectionService = rouletteSelectionService;
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               int numGenerationsIgnored,
                                               String crossoverType) {

        Instant start = Instant.now();

        log.info("🚀 INICIANDO ALGORITMO GENÉTICO PARA FUNCIÓN 5");
        log.info("   Función: f(x) = (x² - 1)² → Buscando MÁXIMO en x = ±3 (f=64)");
        log.info("   Población: {} individuos", POPULATION_SIZE);
        log.info("   Bits (L): {}", L);
        log.info("   Generaciones: {}", NUM_GENERATIONS);
        log.info("   Prob. Cruce: {}%", CROSSOVER_RATE * 100);
        log.info("   Prob. Mutación por bit: {}%", MUTATION_RATE_PER_BIT * 100);
        log.info("   Rango: x ∈ [{}, {}]", xmin, xmax);

        // Generar población inicial
        List<String> currentBinaries = generateInitialPopulation(initialBinaries, L);
        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();

        for (int gen = 0; gen < NUM_GENERATIONS; gen++) {
            log.info(" ");
            log.info("════════════════════════════════════════════════");
            log.info("        🎯 GENERACIÓN {} de {}", gen + 1, NUM_GENERATIONS);
            log.info("════════════════════════════════════════════════");

            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);
            generations.add(generation);
            individualService.saveAll(generation);

            if (gen < NUM_GENERATIONS - 1) {
                log.info("→ SELECCIÓN POR RULETA: Seleccionando {} parejas de padres...", POPULATION_SIZE / 2);
                List<Individual[]> parentPairs = rouletteSelectionService.selectPairs(generation, POPULATION_SIZE / 2);

                log.info("→ CRUCE: Generando {} hijos con cruce de un punto (probabilidad = {}%)",
                        POPULATION_SIZE, CROSSOVER_RATE * 100);
                List<Individual> offspring = new ArrayList<>();
                int crossoverCount = 0;

                for (int i = 0; i < parentPairs.size(); i++) {
                    Individual[] pair = parentPairs.get(i);
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

                    String[] children;
                    if (Math.random() < CROSSOVER_RATE) {
                        children = crossoverService.crossoverWithLogging(bin1, bin2, crossoverType, i+1, L, xmin, xmax);
                        crossoverCount++;
                    } else {
                        children = new String[]{bin1, bin2};
                        log.debug("  Pareja {}: SIN CRUCE (se mantienen padres)", i+1);
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

                log.info("→ MUTACIÓN: Aplicando mutación bit a bit (probabilidad = {}% por bit)", MUTATION_RATE_PER_BIT * 100);
                mutationService.applyToGenerationWithLogging(offspring, MUTATION_RATE_PER_BIT, L, gen + 1);

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

    private List<String> generateInitialPopulation(List<String> initialBinaries, int L) {
        List<String> population = new ArrayList<>();
        Random rand = new Random();

        for (String bin : initialBinaries) {
            if (population.size() < POPULATION_SIZE) {
                population.add(binaryConverterService.normalizeBinary(bin, L));
            }
        }

        while (population.size() < POPULATION_SIZE) {
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
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - 3.0) < 0.1) // x ≈ ±3
                .count();

        double percentage = (double) countConverged / finalGeneration.size() * 100;
        log.info(" ");
        log.info("📊 RESULTADO FINAL DE CONVERGENCIA:");
        log.info("   → Individuos en x ≈ ±3: {} de {}", countConverged, finalGeneration.size());
        log.info("   → Porcentaje: {:.2f}%", percentage);

        if (percentage >= 80) {
            log.info("🎉 ✅ ¡CONVERGENCIA EXITOSA! (≥80% en x ≈ ±3)");
        } else {
            log.warn("⚠️ ❌ Convergencia insuficiente (<80% en x ≈ ±3). Considera ajustar parámetros.");
        }
    }
}