package com.example.demo.service;

import com.example.demo.utils.IndexPair;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrossoverService {

    private final Map<String, CrossoverStrategy> strategies = new HashMap<>();
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;

    public CrossoverService(SinglePointCrossoverStrategy singleStrategy,
                            DoublePointCrossoverStrategy doubleStrategy,
                            BinaryConverterService binaryConverterService,
                            RealConverterService realConverterService,
                            AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
        strategies.put("single", singleStrategy);
        strategies.put("double", doubleStrategy);
    }

    /**
     * Realiza el cruce en una lista de padres según pares de índices y tipo de cruce.
     */
    public List<String> performCrossover(List<String> parentBinaries,
                                         List<IndexPair> crossoverPairs,
                                         double xmin,
                                         double xmax,
                                         int L,
                                         String crossoverType) {

        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));
        List<String> children = new ArrayList<>();

        for (IndexPair pair : crossoverPairs) {
            int i1 = pair.first() - 1;
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= parentBinaries.size() || i2 >= parentBinaries.size()) {
                continue;
            }

            String p1 = binaryConverterService.normalizeBinary(parentBinaries.get(i1), L);
            String p2 = binaryConverterService.normalizeBinary(parentBinaries.get(i2), L);

            String[] result = strategy.crossover(p1, p2);
            children.add(result[0]);
            children.add(result[1]);
        }

        return children;
    }

    // Método auxiliar para calcular fitness (usado en cruce si fuera necesario)
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