package com.example.demo.genetic.operators;

import com.example.demo.conversion.*;
import org.slf4j.*;
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

    public CrossoverService(SinglePointCrossoverStrategy singleStrategy,
                            DoublePointCrossoverStrategy doubleStrategy,
                            UniformCrossoverStrategy uniformStrategy, // Añadir esta línea
                            BinaryConverterService binaryConverterService,
                            RealConverterService realConverterService,
                            AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
        strategies.put("single", singleStrategy);
        strategies.put("double", doubleStrategy);
        strategies.put("uniform", uniformStrategy); // Añadir esta línea
    }

    public CrossoverResult crossoverWithLogging(String parent1, String parent2, String crossoverType,
                                                int pairIndex, int L, double xmin, double xmax, String functionType) {
        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));

        CrossoverResult result = strategy.crossover(parent1, parent2);
        String[] children = result.getChildren();

        // Calcular fitness
        double fitP1 = calculateFitness(parent1, xmin, xmax, L, functionType);
        double fitP2 = calculateFitness(parent2, xmin, xmax, L, functionType);
        double fitH1 = calculateFitness(children[0], xmin, xmax, L, functionType);
        double fitH2 = calculateFitness(children[1], xmin, xmax, L, functionType);

        // Formatear valores de fitness para el log
        String fmtFitP1 = String.format("%.3f", fitP1);
        String fmtFitP2 = String.format("%.3f", fitP2);
        String fmtFitH1 = String.format("%.3f", fitH1);
        String fmtFitH2 = String.format("%.3f", fitH2);

        // Logs específicos por tipo de cruce
        if ("uniform".equals(crossoverType)) {
            log.info("""
                🧬 Pareja {}: Cruce uniforme
                  Padre 1: {} → f(x) = {}
                  Padre 2: {} → f(x) = {}
                  Hijo 1:  {} → f(x) = {}
                  Hijo 2:  {} → f(x) = {}""",
                    pairIndex, parent1, fmtFitP1, parent2, fmtFitP2, children[0], fmtFitH1, children[1], fmtFitH2);
        } else {
            String pointStr = result.getCutPointsString();
            log.info("""
                🧬 Pareja {}: Cruce de un punto
                  Padre 1: {} → f(x) = {}
                  Padre 2: {} → f(x) = {}
                  Punto de corte: {}
                  Hijo 1:  {} → f(x) = {}
                  Hijo 2:  {} → f(x) = {}""",
                    pairIndex, parent1, fmtFitP1, parent2, fmtFitP2,
                    pointStr, children[0], fmtFitH1, children[1], fmtFitH2);
        }

        return result;
    }

    private double calculateFitness(String binary, double xmin, double xmax, int L, String functionType) {
        try {
            int decimal = binaryConverterService.convertBinaryToInt(binary);
            double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
            return adaptiveFunctionService.toAdaptiveSingle(real, functionType);
        } catch (Exception e) {
            log.error("Error calculando fitness para binario {}: {}", binary, e.getMessage());
            return Double.NEGATIVE_INFINITY;
        }
    }
}