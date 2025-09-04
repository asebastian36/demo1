package com.example.demo.service;

import com.example.demo.entities.Individual;
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

    // Planes de cruce por generación
    private final Map<Integer, List<IndexPair>> crossoverPlans = Map.of(
            0, Arrays.asList(
                    new IndexPair(4, 14), new IndexPair(3, 7), new IndexPair(8, 9),
                    new IndexPair(15, 1), new IndexPair(13, 9), new IndexPair(7, 8),
                    new IndexPair(9, 10), new IndexPair(5, 9), new IndexPair(15, 7),
                    new IndexPair(9, 1), new IndexPair(3, 4)
            ),
            1, Arrays.asList(
                    new IndexPair(15, 3), new IndexPair(8, 7), new IndexPair(2, 9),
                    new IndexPair(6, 5), new IndexPair(14, 7), new IndexPair(3, 9),
                    new IndexPair(13, 4), new IndexPair(9, 2), new IndexPair(15, 9),
                    new IndexPair(7, 3), new IndexPair(8, 12)
            )
    );

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   IndividualService individualService) {
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.individualService = individualService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               int numGenerations,
                                               String crossoverType) {

        log.info("Inicio del algoritmo genético: {} generaciones, L={}, crossover={}", numGenerations, L, crossoverType);
        log.debug("Población inicial: {}", initialBinaries);

        List<List<Individual>> generations = new ArrayList<>();
        List<String> currentBinaries = binaryConverterService.normalizeAllBinaries(initialBinaries, L);

        for (int gen = 0; gen < numGenerations; gen++) {
            log.info("=== GENERACIÓN {} ===", gen + 1);

            // Crear y ordenar por adaptativo descendente
            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);
            generations.add(generation);
            individualService.saveAll(generation);

            log.debug("Generación {} creada y guardada: {} individuos", gen + 1, generation.size());

            if (gen < numGenerations - 1) {
                // Obtener pares de cruce según generación
                List<IndexPair> crossoverPairs = crossoverPlans.getOrDefault(gen, Collections.emptyList());
                log.debug("Aplicando {} cruces en generación {}", crossoverPairs.size(), gen + 1);

                // Aplicar cruce directo (reemplazo posicional)
                crossoverService.applyCrossover(generation, crossoverPairs, xmin, xmax, L, crossoverType);

                // Aplicar mutación aleatoria in-situ
                mutationService.applyToGeneration(generation, 0.02, L);
                log.debug("Mutación aplicada a generación {}", gen + 1);

                // Actualizar binarios para próxima generación
                currentBinaries = generation.stream()
                        .map(Individual::getBinary)
                        .collect(Collectors.toList());
            }
        }

        log.info("Algoritmo finalizado. Total generaciones: {}", generations.size());
        return generations;
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
}