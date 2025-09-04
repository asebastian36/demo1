package com.example.demo.service;

import com.example.demo.utils.IndexPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CrossoverService {

    private static final Logger log = LoggerFactory.getLogger(CrossoverService.class);
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

        log.debug("Iniciando cruce con estrategia: {}", crossoverType);
        log.trace("Padres: {}", parentBinaries);
        log.trace("Pares de cruce: {}", crossoverPairs);

        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));
        List<String> children = new ArrayList<>();

        for (IndexPair pair : crossoverPairs) {
            int i1 = pair.first() - 1;
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= parentBinaries.size() || i2 >= parentBinaries.size()) {
                log.warn("Par de cruce inválido ignorado: {}", pair);
                continue;
            }

            String p1 = binaryConverterService.normalizeBinary(parentBinaries.get(i1), L);
            String p2 = binaryConverterService.normalizeBinary(parentBinaries.get(i2), L);

            String[] result = strategy.crossover(p1, p2);
            children.add(result[0]);
            children.add(result[1]);

            log.trace("Cruce {}×{} → H1: {}, H2: {}", p1, p2, result[0], result[1]);
        }

        log.debug("Cruce finalizado: {} hijos generados", children.size());
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