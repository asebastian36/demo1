package com.example.demo.service;

import com.example.demo.entities.Individual;
import com.example.demo.repository.IndividualRepository;
import com.example.demo.utils.IndexPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmService {

    private final CrossoverService crossoverService;
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private final IndividualRepository individualRepository;

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

    public GeneticAlgorithmService(CrossoverService crossoverService,
                                   BinaryConverterService binaryConverterService,
                                   RealConverterService realConverterService,
                                   AdaptiveFunctionService adaptiveFunctionService,
                                   IndividualRepository individualRepository) {
        this.crossoverService = crossoverService;
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.individualRepository = individualRepository;
    }

    @Transactional
    public List<List<Individual>> runEvolution(List<String> initialBinaries,
                                               double xmin,
                                               double xmax,
                                               int L,
                                               int numGenerations,
                                               String crossoverType) {
        // Opcional: limpiar generaciones anteriores
        // individualRepository.deleteAll(); // descomenta si quieres borrar todo

        List<List<Individual>> generations = new ArrayList<>();
        List<String> currentBinaries = binaryConverterService.normalizeAllBinaries(initialBinaries, L);

        for (int gen = 0; gen < numGenerations; gen++) {
            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen);
            generations.add(generation);

            // Guardar en BD
            individualRepository.saveAll(generation);

            if (gen < numGenerations - 1) {
                List<String> orderedBinaries = generation.stream()
                        .map(Individual::getBinary)
                        .collect(Collectors.toList());

                List<IndexPair> crossoverPlan = crossoverPlans.getOrDefault(gen, Collections.emptyList());
                List<IndexPair> mappedCrossovers = mapCrossoverIndices(crossoverPlan, orderedBinaries);

                currentBinaries = crossoverService.performCrossover(orderedBinaries, mappedCrossovers, xmin, xmax, L, crossoverType);
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

    private List<IndexPair> mapCrossoverIndices(List<IndexPair> originalPairs, List<String> orderedBinaries) {
        return originalPairs.stream()
                .filter(pair -> pair.first() <= orderedBinaries.size() && pair.second() <= orderedBinaries.size())
                .collect(Collectors.toList());
    }
}