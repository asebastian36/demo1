package com.example.demo.service.crossover;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.AdaptiveFunctionService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.conversion.RealConverterService;
import com.example.demo.utils.IndexPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrossoverService {

    private static final Logger log = LoggerFactory.getLogger(CrossoverService.class);

    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private final Map<String, CrossoverStrategy> strategies = new HashMap<>();

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
     * Aplica cruces directamente sobre la población: reemplaza solo si el hijo es mejor que su padre.
     */
    public void applyCrossover(List<Individual> population,
                               List<IndexPair> pairs,
                               double xmin,
                               double xmax,
                               int L,
                               String crossoverType) {

        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));

        log.debug("Aplicando {} cruces con estrategia: {}", pairs.size(), crossoverType);

        for (IndexPair pair : pairs) {
            int i1 = pair.first() - 1;  // Convertir a índice 0-based
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= population.size() || i2 >= population.size()) {
                log.warn("Par de cruce inválido ignorado: {} → índices {},{}", pair, i1, i2);
                continue;
            }

            Individual parent1 = population.get(i1);
            Individual parent2 = population.get(i2);

            String bin1 = binaryConverterService.normalizeBinary(parent1.getBinary(), L);
            String bin2 = binaryConverterService.normalizeBinary(parent2.getBinary(), L);

            String[] children = strategy.crossover(bin1, bin2);
            String child1 = children[0];
            String child2 = children[1];

            double parent1Fit = parent1.getAdaptative();
            double parent2Fit = parent2.getAdaptative();

            double child1Fit = calculateFitness(child1, xmin, xmax, L);
            double child2Fit = calculateFitness(child2, xmin, xmax, L);

            log.trace("Cruce {}×{} → H1:{}(f={:.3f}) vs P1:{}(f={:.3f}) | H2:{}(f={:.3f}) vs P2:{}(f={:.3f})",
                    bin1, bin2, child1, child1Fit, parent1.getBinary(), parent1Fit,
                    child2, child2Fit, parent2.getBinary(), parent2Fit);

            // Reemplazar solo si el hijo es mejor
            if (child1Fit > parent1Fit) {
                double real = realConverterService.toRealSingle(
                        binaryConverterService.convertBinaryToInt(child1), xmin, xmax, L);
                population.set(i1, new Individual(child1, real, child1Fit, parent1.getGeneration()));
                log.debug("Hijo 1 reemplaza a Padre 1 en posición {}", i1 + 1);
            }

            if (child2Fit > parent2Fit) {
                double real = realConverterService.toRealSingle(
                        binaryConverterService.convertBinaryToInt(child2), xmin, xmax, L);
                population.set(i2, new Individual(child2, real, child2Fit, parent2.getGeneration()));
                log.debug("Hijo 2 reemplaza a Padre 2 en posición {}", i2 + 1);
            }
        }
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