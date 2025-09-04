package com.example.demo.service;

import com.example.demo.utils.IndexPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrossoverService {

    private final Map<String, CrossoverStrategy> strategies = new HashMap<>();

    @Autowired
    private BinaryConverterService binaryConverterService;

    @Autowired
    private RealConverterService realConverterService;

    @Autowired
    private AdaptiveFunctionService adaptiveFunctionService;


    // Inyectamos ambas estrategias
    public CrossoverService(SinglePointCrossoverStrategy singleStrategy,
                            DoublePointCrossoverStrategy doubleStrategy) {
        strategies.put("single", singleStrategy);
        strategies.put("double", doubleStrategy);
    }

    public List<String> performCrossover(List<String> binaries,
                                         List<IndexPair> crossovers,
                                         double xmin, double xmax, int L,
                                         String crossoverType) { // "single" o "double"

        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));
        List<String> nextGeneration = new ArrayList<>(binaries);

        for (IndexPair pair : crossovers) {
            int i1 = pair.first() - 1;
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= binaries.size() || i2 >= binaries.size()) continue;

            String parent1 = binaryConverterService.normalizeBinary(binaries.get(i1), L);
            String parent2 = binaryConverterService.normalizeBinary(binaries.get(i2), L);

            String[] children = strategy.crossover(parent1, parent2);
            double fit1 = calculateFitness(binaries.get(i1), xmin, xmax, L);
            double fit2 = calculateFitness(binaries.get(i2), xmin, xmax, L);
            double childFit1 = calculateFitness(children[0], xmin, xmax, L);
            double childFit2 = calculateFitness(children[1], xmin, xmax, L);

            if (childFit1 > fit1) nextGeneration.set(i1, children[0]);
            if (childFit2 > fit2) nextGeneration.set(i2, children[1]);
        }

        return nextGeneration;
    }

    private double calculateFitness(String binary, double xmin, double xmax, int L) {
        try {
            int decimal = binaryConverterService.convertBinaryToInt(binary);
            double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
            return adaptiveFunctionService.toAdaptiveSingle(real);
        } catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }
}