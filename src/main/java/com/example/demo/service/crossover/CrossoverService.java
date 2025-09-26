package com.example.demo.service.crossover;

import com.example.demo.service.conversion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.*;

@Service
public class CrossoverService {

    private static final Logger log = LoggerFactory.getLogger(CrossoverService.class);

    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private final Map<String, CrossoverStrategy> strategies = new HashMap<>();
    private final Random random = new Random();

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

    // Método nuevo: cruce con logs y functionType
    public String[] crossoverWithLogging(String parent1, String parent2, String crossoverType,
                                         int pairIndex, int L, double xmin, double xmax, String functionType) {
        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));

        int point = -1;
        if (strategy instanceof SinglePointCrossoverStrategy) {
            point = 1 + random.nextInt(L - 1);
            ((SinglePointCrossoverStrategy) strategy).setForcedPoint(point);
        }

        String[] children = strategy.crossover(parent1, parent2);

        if (strategy instanceof SinglePointCrossoverStrategy) {
            ((SinglePointCrossoverStrategy) strategy).clearForcedPoint();
        }

        // Calcular fitness usando functionType
        double fitP1 = calculateFitness(parent1, xmin, xmax, L, functionType);
        double fitP2 = calculateFitness(parent2, xmin, xmax, L, functionType);
        double fitH1 = calculateFitness(children[0], xmin, xmax, L, functionType);
        double fitH2 = calculateFitness(children[1], xmin, xmax, L, functionType);

        log.info("""
                🧬 Pareja {}: Cruce de un punto
                  Padre 1: {} → f(x) = {:.3f}
                  Padre 2: {} → f(x) = {:.3f}
                  Punto de corte: {}
                  Hijo 1:  {} → f(x) = {:.3f}
                  Hijo 2:  {} → f(x) = {:.3f}""",
                pairIndex,
                parent1, fitP1,
                parent2, fitP2,
                point == -1 ? "N/A" : point,
                children[0], fitH1,
                children[1], fitH2);

        return children;
    }

    // Método corregido: ahora requiere functionType
    private double calculateFitness(String binary, double xmin, double xmax, int L, String functionType) {
        try {
            int decimal = binaryConverterService.convertBinaryToInt(binary);
            double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
            return adaptiveFunctionService.toAdaptiveSingle(real, functionType); // ✅ Ahora con functionType
        } catch (Exception e) {
            log.error("Error calculando fitness para binario {}: {}", binary, e.getMessage());
            return Double.NEGATIVE_INFINITY;
        }
    }
}