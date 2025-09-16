package com.example.demo.service.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.service.crossover.CrossoverService;
import com.example.demo.service.conversion.*;
import com.example.demo.service.mutation.MutationService;
import com.example.demo.service.persistence.IndividualService;
import com.example.demo.service.selection.RouletteSelectionService;
import com.example.demo.utils.IndexPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RouletteSelectionService rouletteSelectionService; // NUEVO

    private static final int POPULATION_SIZE = 4200;
    private static final int NUM_GENERATIONS = 1000; // Puedes hacerlo configurable
    private static final double MUTATION_RATE_PER_BIT = 0.001; // 0.1% por bit
    private static final double CROSSOVER_RATE = 0.8; // 80% de parejas cruzan

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   IndividualService individualService,
                                   RouletteSelectionService rouletteSelectionService) { // Añadido
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.individualService = individualService;
        this.rouletteSelectionService = rouletteSelectionService; // Inyectado
        this.mutationService.setBounds(-3, 3); // Por defecto, ajustable
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               int numGenerationsIgnored, // Ignoramos este parámetro
                                               String crossoverType) {

        log.info("Inicio del algoritmo genético: {} generaciones, L={}, crossover={}", NUM_GENERATIONS, L, crossoverType);

        // 1. Generar población inicial (4200 individuos)
        List<String> currentBinaries = generateInitialPopulation(initialBinaries, L);

        // Setear bounds para mutación
        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();

        for (int gen = 0; gen < NUM_GENERATIONS; gen++) {
            log.info("=== GENERACIÓN {} ===", gen + 1);

            // Crear generación actual (ordenada)
            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);
            generations.add(generation);
            individualService.saveAll(generation);

            if (gen < NUM_GENERATIONS - 1) {
                // 2. Selección por ruleta → padres
                List<Individual[]> parentPairs = rouletteSelectionService.selectPairs(generation, POPULATION_SIZE / 2);

                // 3. Cruce → hijos
                List<Individual> offspring = new ArrayList<>();
                Random rand = new Random();

                for (Individual[] pair : parentPairs) {
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

                    String[] children;
                    if (rand.nextDouble() < CROSSOVER_RATE) {
                        children = crossoverService.crossover(bin1, bin2, crossoverType); // Método modificado abajo
                    } else {
                        children = new String[]{bin1, bin2}; // Sin cruce
                    }

                    // Crear hijos (sin evaluar aún)
                    for (String childBinary : children) {
                        int decimal = binaryConverterService.convertBinaryToInt(childBinary);
                        double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                        double adaptative = adaptiveFunctionService.toAdaptiveSingle(real);
                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }

                // 4. Mutación
                mutationService.applyToGeneration(offspring, MUTATION_RATE_PER_BIT, L);

                // 5. Reemplazo: nueva generación = hijos
                currentBinaries = offspring.stream()
                        .map(Individual::getBinary)
                        .collect(Collectors.toList());
            }
        }

        // 6. Verificar convergencia (≥80% en x ≈ ±1)
        verifyConvergence(generations.get(generations.size() - 1));

        log.info("Algoritmo finalizado. Total generaciones: {}", generations.size());
        return generations;
    }

    private List<String> generateInitialPopulation(List<String> initialBinaries, int L) {
        List<String> population = new ArrayList<>();
        Random rand = new Random();

        // Usar los binarios iniciales si existen
        for (String bin : initialBinaries) {
            if (population.size() < POPULATION_SIZE) {
                population.add(binaryConverterService.normalizeBinary(bin, L));
            }
        }

        // Completar con aleatorios si es necesario
        while (population.size() < POPULATION_SIZE) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < L; i++) {
                sb.append(rand.nextBoolean() ? '1' : '0');
            }
            population.add(sb.toString());
        }

        log.info("Población inicial generada: {} individuos", population.size());
        return population;
    }

    private void verifyConvergence(List<Individual> finalGeneration) {
        long countConverged = finalGeneration.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - 3.0) < 0.1) // x ≈ ±3
                .count();

        double percentage = (double) countConverged / finalGeneration.size() * 100;
        log.info("Convergencia final: {}% en x ≈ ±3 ({} de {} individuos)",
                String.format("%.2f", percentage), countConverged, finalGeneration.size());

        if (percentage >= 80) {
            log.info("✅ ¡Convergencia exitosa! (≥80% en x ≈ ±3)");
        } else {
            log.warn("⚠️ Convergencia insuficiente (<80% en x ≈ ±3)");
        }
    }
    private List<Individual> createOrderedGeneration(List<String> binaries, double xmin, double xmax, int L, int generationIndex) {
        List<Integer> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
        List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
        List<Double> fitnessValues = adaptiveFunctionService.toAdaptive(reals);

        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < binaries.size(); i++) {
            individuals.add(new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generationIndex));
        }

        // Ordenar por adaptativo descendente
        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());

        log.trace("Generación {} ordenada: {}", generationIndex + 1,
                individuals.stream()
                        .map(i -> i.getBinary() + "(" + String.format("%.2f", i.getAdaptative()) + ")")
                        .collect(Collectors.toList()));

        return individuals;
    }
    // ... (el resto de métodos como createOrderedGeneration se mantienen igual)
}
