package com.example.demo.execution.strategy.crossover;

import com.example.demo.function.FitnessFunction;
import com.example.demo.function.ChromosomeBasedFitnessFunction;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.io.conversion.DecimalToRealConverter;
import com.example.demo.io.conversion.FitnessEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CrossoverService {

    private static final Logger log = LoggerFactory.getLogger(CrossoverService.class);

    private final BinaryToDecimalConverter binaryConverter;
    private final DecimalToRealConverter realConverter;
    private final FitnessEvaluator fitnessEvaluator;
    private final Map<String, CrossoverStrategy> strategies = new HashMap<>();

    public CrossoverService(SinglePointCrossoverStrategy singleStrategy,
                            DoublePointCrossoverStrategy doubleStrategy,
                            UniformCrossoverStrategy uniformStrategy,
                            BinaryToDecimalConverter binaryConverter,
                            DecimalToRealConverter realConverter,
                            FitnessEvaluator fitnessEvaluator) {
        this.binaryConverter = binaryConverter;
        this.realConverter = realConverter;
        this.fitnessEvaluator = fitnessEvaluator;
        strategies.put("single", singleStrategy);
        strategies.put("double", doubleStrategy);
        strategies.put("uniform", uniformStrategy);
    }

    public CrossoverResult crossoverWithLogging(String parent1, String parent2, String crossoverType,
                                                int pairIndex, int L, double xmin, double xmax, String functionType) {
        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));
        CrossoverResult result = strategy.crossover(parent1, parent2);

        if (log.isDebugEnabled()) {
            String[] children = result.getChildren();
            double fitP1 = calculateFitness(parent1, xmin, xmax, L, functionType);
            double fitP2 = calculateFitness(parent2, xmin, xmax, L, functionType);
            double fitH1 = calculateFitness(children[0], xmin, xmax, L, functionType);
            double fitH2 = calculateFitness(children[1], xmin, xmax, L, functionType);

            String fmtFitP1 = fitP1 == Double.NEGATIVE_INFINITY ? "Error" : String.format("%.3f", fitP1);
            String fmtFitP2 = fitP2 == Double.NEGATIVE_INFINITY ? "Error" : String.format("%.3f", fitP2);
            String fmtFitH1 = fitH1 == Double.NEGATIVE_INFINITY ? "Error" : String.format("%.3f", fitH1);
            String fmtFitH2 = fitH2 == Double.NEGATIVE_INFINITY ? "Error" : String.format("%.3f", fitH2);

            if ("uniform".equals(crossoverType)) {
                log.debug("""
                    🧬 Pareja {}: Cruce uniforme
                      Padre 1: {} → f(x) = {}
                      Padre 2: {} → f(x) = {}
                      Hijo 1:  {} → f(x) = {}
                      Hijo 2:  {} → f(x) = {}""",
                        pairIndex, parent1, fmtFitP1, parent2, fmtFitP2, children[0], fmtFitH1, children[1], fmtFitH2);
            } else {
                String pointStr = result.getCutPointsString();
                log.debug("""
                    🧬 Pareja {}: Cruce de un punto
                      Padre 1: {} → f(x) = {}
                      Padre 2: {} → f(x) = {}
                      Punto de corte: {}
                      Hijo 1:  {} → f(x) = {}
                      Hijo 2:  {} → f(x) = {}""",
                        pairIndex, parent1, fmtFitP1, parent2, fmtFitP2,
                        pointStr, children[0], fmtFitH1, children[1], fmtFitH2);
            }
        }

        return result;
    }

    private double calculateFitness(String binary, double xmin, double xmax, int L, String functionType) {
        try {
            if ("credit".equals(functionType) || "consumo".equals(functionType)) {
                FitnessFunction function = fitnessEvaluator.getFunction(functionType);
                if (function instanceof ChromosomeBasedFitnessFunction) {
                    return function.evaluate(binary);
                }
            }

            long decimal = binaryConverter.convertBinaryToInt(binary);
            double real = realConverter.toRealSingle(decimal, xmin, xmax, L);
            return fitnessEvaluator.toAdaptiveSingle(real, functionType);
        } catch (Exception e) {
            log.error("Error calculando fitness para binario {}: {}", binary, e.getMessage());
            return Double.NEGATIVE_INFINITY;
        }
    }
}