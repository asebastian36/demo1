package com.example.demo.service;

import com.example.demo.utils.IndexPair;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrossoverService {

    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;

    public CrossoverService(BinaryConverterService binaryConverterService,
                            RealConverterService realConverterService,
                            AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
    }

    public List<String> performCrossover(List<String> binaries, List<IndexPair> crossovers,
                                         double xmin, double xmax, int L) {
        List<String> nextGeneration = new ArrayList<>(binaries);

        for (IndexPair pair : crossovers) {
            int i1 = pair.first() - 1;
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= binaries.size() || i2 >= binaries.size()) continue;

            String parent1 = binaryConverterService.normalizeBinary(binaries.get(i1), L);
            String parent2 = binaryConverterService.normalizeBinary(binaries.get(i2), L);

            String[] children = crossoverAtBit4(parent1, parent2);
            double fit1 = calculateFitness(binaries.get(i1), xmin, xmax, L);
            double fit2 = calculateFitness(binaries.get(i2), xmin, xmax, L);
            double childFit1 = calculateFitness(children[0], xmin, xmax, L);
            double childFit2 = calculateFitness(children[1], xmin, xmax, L);

            if (childFit1 > fit1) nextGeneration.set(i1, children[0]);
            if (childFit2 > fit2) nextGeneration.set(i2, children[1]);
        }

        return nextGeneration;
    }

    private String[] crossoverAtBit4(String b1, String b2) {
        if (b1.length() != b2.length()) {
            throw new IllegalArgumentException("Binarios de longitud diferente: " + b1 + ", " + b2);
        }
        String c1 = b2.substring(0, 4) + b1.substring(4);
        String c2 = b1.substring(0, 4) + b2.substring(4);
        return new String[]{c1, c2};
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