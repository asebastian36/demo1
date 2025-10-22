package com.example.demo.execution.strategy.mutation;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.FitnessFunction;
import com.example.demo.function.credit.CreditRiskFitnessFunction;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.io.conversion.DecimalToRealConverter;
import com.example.demo.io.conversion.FitnessEvaluator;
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

    private final BinaryToDecimalConverter binaryConverter;
    private final DecimalToRealConverter realConverter;
    private final FitnessEvaluator fitnessEvaluator;
    private final Map<String, MutationStrategy> mutationStrategies;

    private double xmin, xmax;

    public MutationService(
            BinaryToDecimalConverter binaryConverter,
            DecimalToRealConverter realConverter,
            FitnessEvaluator fitnessEvaluator,
            Map<String, MutationStrategy> mutationStrategies) {
        this.binaryConverter = binaryConverter;
        this.realConverter = realConverter;
        this.fitnessEvaluator = fitnessEvaluator;
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
                    FitnessFunction function = fitnessEvaluator.getFunction(functionType);
                    if (!(function instanceof CreditRiskFitnessFunction)) {
                        throw new IllegalStateException("Función de crédito no disponible para re-evaluación.");
                    }
                    adaptative = function.evaluate(mutatedBinary);
                    real = 0.0;
                } else {
                    long decimal = binaryConverter.convertBinaryToInt(mutatedBinary);
                    real = realConverter.toRealSingle(decimal, xmin, xmax, L);
                    adaptative = fitnessEvaluator.toAdaptiveSingle(real, functionType);
                }

                Individual mutated = new Individual(mutatedBinary, real, adaptative, original.getGeneration());
                generation.set(i, mutated);
                mutatedIndividuals++;
            }
        }

        log.info("→ Mutación ({}) en generación {}: {} individuos mutados ({}%)",
                strategy.getName(), gen,
                mutatedIndividuals,
                String.format("%.2f", (double) mutatedIndividuals / generation.size() * 100));
    }
}