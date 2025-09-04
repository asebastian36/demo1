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
    private final CrossoverService crossoverService;
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private final IndividualService individualService;
    private final RouletteSelectionService rouletteSelectionService;
    private final MutationService mutationService;

    // Tasa de mutación (2%)
    private static final double MUTATION_RATE = 0.02;

    public GeneticAlgorithmService(CrossoverService crossoverService,
                                   BinaryConverterService binaryConverterService,
                                   RealConverterService realConverterService,
                                   AdaptiveFunctionService adaptiveFunctionService,
                                   IndividualService individualService,
                                   RouletteSelectionService rouletteSelectionService,
                                   MutationService mutationService) {
        this.crossoverService = crossoverService;
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.individualService = individualService;
        this.rouletteSelectionService = rouletteSelectionService;
        this.mutationService = mutationService;
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               int numGenerations,
                                               String crossoverType) {

        log.info("Inicio del algoritmo genético: {} generaciones, L={}, crossover={}", numGenerations, L, crossoverType);
        log.debug("Parámetros: xmin={}, xmax={}", xmin, xmax);
        log.debug("Población inicial: {}", initialBinaries);

        List<List<Individual>> generations = new ArrayList<>();
        List<String> currentBinaries = binaryConverterService.normalizeAllBinaries(initialBinaries, L);

        log.debug("Binarios normalizados a longitud {}: {}", L, currentBinaries);

        for (int gen = 0; gen < numGenerations; gen++) {

            log.info("=== GENERACIÓN {} ===", gen + 1);

            // 1. Crear generación actual
            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);

            log.debug("Generación {} creada: {} individuos", gen + 1, generation.size());
            log.trace("Individuos ordenados: {}", generation.stream()
                    .map(i -> i.getBinary() + " → f(x)=" + String.format("%.3f", i.getAdaptative()))
                    .collect(Collectors.toList()));

            generations.add(generation);
            individualService.saveAll(generation);

            log.debug("Generación {} guardada en BD", gen + 1);

            if (gen < numGenerations - 1) {
                // 2. Seleccionar pares de padres por ruleta (11 pares → 22 padres)
                List<Individual[]> parentPairs = rouletteSelectionService.selectPairs(generation, 11);

                log.debug("Seleccionados {} pares de padres por ruleta", parentPairs.size());

                // 3. Extraer binarios de los padres seleccionados
                List<String> parentBinaries = new ArrayList<>();
                List<IndexPair> crossoverPairs = new ArrayList<>();
                int idx = 1;
                for (Individual[] pair : parentPairs) {
                    parentBinaries.add(pair[0].getBinary());
                    parentBinaries.add(pair[1].getBinary());
                    crossoverPairs.add(new IndexPair(idx, idx + 1));
                    idx += 2;
                }

                log.debug("Padres para cruce: {}", parentBinaries);

                // 4. Aplicar cruce
                List<String> childrenBinaries = crossoverService.performCrossover(
                        parentBinaries, crossoverPairs, xmin, xmax, L, crossoverType
                );

                log.debug("Hijos generados ({}): {}", childrenBinaries.size(), childrenBinaries);

                // 5. Crear individuos hijos y ordenar por adaptativo
                List<Individual> children = createIndividualsFromBinaries(childrenBinaries, xmin, xmax, L, gen + 1);

                // 6. Aplicar mutación aleatoria
                List<Individual> mutatedChildren = mutationService.mutateAll(children, MUTATION_RATE, L);

                // 7. Ordenar por adaptativo descendente y quedarse con los mejores 15
                List<String> nextGeneration = mutatedChildren.stream()
                        .sorted(Comparator.comparingDouble(Individual::getAdaptative).reversed())
                        .limit(15)
                        .map(Individual::getBinary)
                        .collect(Collectors.toList());

                // 8. Si faltan individuos, rellenar con los mejores de la generación actual
                while (nextGeneration.size() < 15) {
                    nextGeneration.add(generation.get(nextGeneration.size()).getBinary());
                }

                currentBinaries = nextGeneration;
            }
        }

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

        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());
        return individuals;
    }

    private List<Individual> createIndividualsFromBinaries(List<String> binaries, double xmin, double xmax, int L, int gen) {
        return binaries.stream().map(bin -> {
            try {
                int dec = binaryConverterService.convertBinaryToInt(bin);
                double real = realConverterService.toRealSingle(dec, xmin, xmax, L);
                double fit = adaptiveFunctionService.toAdaptiveSingle(real);
                return new Individual(bin, real, fit, gen);
            } catch (Exception e) {
                return new Individual(bin, 0.0, Double.NEGATIVE_INFINITY, gen);
            }
        }).collect(Collectors.toList());
    }
}