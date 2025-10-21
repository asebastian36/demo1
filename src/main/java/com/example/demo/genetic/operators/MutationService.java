package com.example.demo.genetic.operators;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.*;
import com.example.demo.genetic.function.CreditFitnessFunction;
import com.example.demo.genetic.function.FitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class MutationService {

    private static final Logger log = LoggerFactory.getLogger(MutationService.class);
    private final Random random = new Random();

    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private final Map<String, MutationStrategy> mutationStrategies;

    private double xmin, xmax;

    public MutationService(
            BinaryConverterService binaryConverterService,
            RealConverterService realConverterService,
            AdaptiveFunctionService adaptiveFunctionService,
            Map<String, MutationStrategy> mutationStrategies) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.mutationStrategies = mutationStrategies;
    }

    public void setBounds(double xmin, double xmax) {
        this.xmin = xmin;
        this.xmax = xmax;
    }

    public void applyToGenerationWithLogging(
            List<Individual> generation,
            double mutationRate,
            int L,
            int gen,
            String mutationType,
            String functionType) {

        MutationStrategy strategy = mutationStrategies.get(mutationType);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de mutación desconocido: " + mutationType);
        }

        int mutatedIndividuals = 0;

        for (int i = 0; i < generation.size(); i++) {
            Individual original = generation.get(i);
            String originalBinary = original.getBinary();
            String mutatedBinary = strategy.mutate(originalBinary, mutationRate, L);

            if (!originalBinary.equals(mutatedBinary)) {
                double real;
                double adaptative;

                if ("credit".equals(functionType)) {
                    FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
                    if (!(function instanceof CreditFitnessFunction)) {
                        throw new IllegalStateException("Función de crédito no disponible para re-evaluación.");
                    }
                    adaptative = function.evaluate(mutatedBinary);
                    real = 0.0;
                } else {
                    long decimal = binaryConverterService.convertBinaryToInt(mutatedBinary);
                    real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                    adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
                }

                Individual mutated = new Individual(mutatedBinary, real, adaptative, original.getGeneration());
                generation.set(i, mutated);
                mutatedIndividuals++;
            }
        }

        // 🚨 Solo log resumen, sin detalles por individuo
        log.info("→ Mutación ({}) en generación {}: {} individuos mutados ({}%)",
                strategy.getName(), gen,
                mutatedIndividuals,
                String.format("%.2f", (double) mutatedIndividuals / generation.size() * 100));
    }
}