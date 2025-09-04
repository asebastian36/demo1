package com.example.demo.service.crossover;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.*;
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

    public void applyCrossover(List<Individual> population,
                               List<IndexPair> pairs,
                               double xmin,
                               double xmax,
                               int L,
                               String crossoverType) {

        CrossoverStrategy strategy = strategies.getOrDefault(crossoverType, strategies.get("single"));

        for (IndexPair pair : pairs) {
            int i1 = pair.first() - 1;
            int i2 = pair.second() - 1;

            if (i1 < 0 || i2 < 0 || i1 >= population.size() || i2 >= population.size()) {
                log.warn("❌ Cruce ({}, {}) ignorado: índices fuera de rango", pair.first(), pair.second());
                continue;
            }

            Individual p1 = population.get(i1);
            Individual p2 = population.get(i2);

            String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
            String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

            String[] children = strategy.crossover(bin1, bin2);
            String h1 = children[0];
            String h2 = children[1];

            double fitP1 = p1.getAdaptative();
            double fitP2 = p2.getAdaptative();
            double fitH1 = calculateFitness(h1, xmin, xmax, L);
            double fitH2 = calculateFitness(h2, xmin, xmax, L);

            // 🔥 Formato claro y detallado
            log.info("""
                    
                    🧬 Cruce ({}, {})
                      P1: {} - {:.3f}
                      P2: {} - {:.3f}
                      H1: {} - {:.3f}
                      H2: {} - {:.3f}"""
                    , pair.first(), pair.second()
                    , bin1, fitP1
                    , bin2, fitP2
                    , h1, fitH1
                    , h2, fitH2);

            boolean replaceP1 = fitH1 > fitP1;
            boolean replaceP2 = fitH2 > fitP2;

            if (replaceP1) {
                double real = realConverterService.toRealSingle(
                        binaryConverterService.convertBinaryToInt(h1), xmin, xmax, L);
                population.set(i1, new Individual(h1, real, fitH1, p1.getGeneration()));
                log.info("  ✅ H1 reemplaza a P1 ({:.3f} > {:.3f})", fitH1, fitP1);
            } else {
                log.info("  ❌ H1 no reemplaza a P1 ({:.3f} ≤ {:.3f})", fitH1, fitP1);
            }

            if (replaceP2) {
                double real = realConverterService.toRealSingle(
                        binaryConverterService.convertBinaryToInt(h2), xmin, xmax, L);
                population.set(i2, new Individual(h2, real, fitH2, p2.getGeneration()));
                log.info("  ✅ H2 reemplaza a P2 ({:.3f} > {:.3f})", fitH2, fitP2);
            } else {
                log.info("  ❌ H2 no reemplaza a P2 ({:.3f} ≤ {:.3f})", fitH2, fitP2);
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