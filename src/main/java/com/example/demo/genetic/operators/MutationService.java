package com.example.demo.genetic.operators;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.*;
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

    /**
     * Aplica mutación a toda la generación usando la estrategia seleccionada.
     */
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

        log.info("→ Iniciando mutación ({}) en generación {} (tasa: {}%)",
                strategy.getName(), gen, mutationRate * 100);

        int mutatedIndividuals = 0;

        for (int i = 0; i < generation.size(); i++) {
            Individual original = generation.get(i);
            String originalBinary = original.getBinary();

            String mutatedBinary = strategy.mutate(originalBinary, mutationRate, L);

            if (!originalBinary.equals(mutatedBinary)) {
                int decimal = binaryConverterService.convertBinaryToInt(mutatedBinary);
                double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                double adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);

                Individual mutated = new Individual(mutatedBinary, real, adaptative, original.getGeneration());
                generation.set(i, mutated);
                mutatedIndividuals++;

                log.debug("  ✅ Individuo {} mutado: {} → {} (f(x) = {})",
                        i + 1, originalBinary, mutatedBinary, String.format("%.3f", adaptative));
            }
        }

        log.info("→ 🧬 Mutación ({}) finalizada en generación {}: {} individuos mutados ({}%)",
                strategy.getName(), gen,
                mutatedIndividuals,
                String.format("%.2f", (double) mutatedIndividuals / generation.size() * 100));
    }
}