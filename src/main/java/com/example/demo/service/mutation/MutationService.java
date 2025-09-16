package com.example.demo.service.mutation;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.AdaptiveFunctionService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.conversion.RealConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MutationService {

    private static final Logger log = LoggerFactory.getLogger(MutationService.class);
    private final Random random = new Random();

    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private double xmin, xmax;

    public MutationService(BinaryConverterService binaryConverterService,
                           RealConverterService realConverterService,
                           AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
    }

    public void setBounds(double xmin, double xmax) {
        this.xmin = xmin;
        this.xmax = xmax;
    }

    public void applyToGenerationWithLogging(List<Individual> generation, double mutationRatePerBit, int L, int gen) {
        log.info("→ Iniciando mutación en generación {} (tasa por bit: {}%)", gen, mutationRatePerBit * 100);
        int totalMutations = 0;
        int mutatedIndividuals = 0;

        for (int i = 0; i < generation.size(); i++) {
            Individual original = generation.get(i);
            String originalBinary = original.getBinary();
            StringBuilder sb = new StringBuilder(originalBinary);
            boolean individualMutated = false;

            for (int bitIndex = 0; bitIndex < sb.length(); bitIndex++) {
                if (random.nextDouble() < mutationRatePerBit) {
                    char oldBit = sb.charAt(bitIndex);
                    char newBit = oldBit == '0' ? '1' : '0';
                    sb.setCharAt(bitIndex, newBit);
                    totalMutations++;
                    individualMutated = true;

                    log.trace("  🧬 Mutación en individuo {} (gen {}), bit {}: {} → {}",
                            i + 1, gen, bitIndex + 1, oldBit, newBit);
                }
            }

            if (individualMutated) {
                String mutatedBinary = sb.toString();
                int decimal = binaryConverterService.convertBinaryToInt(mutatedBinary);
                double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                double adaptative = adaptiveFunctionService.toAdaptiveSingle(real);

                Individual mutated = new Individual(mutatedBinary, real, adaptative, original.getGeneration());
                generation.set(i, mutated);
                mutatedIndividuals++;

                log.debug("  ✅ Individuo {} mutado: {} → {} (f(x) = {:.3f})",
                        i + 1, originalBinary, mutatedBinary, adaptative);
            }
        }

        log.info("→ 🧬 Mutación finalizada en generación {}: {} individuos mutados ({}%), {} mutaciones totales",
                gen,
                mutatedIndividuals,
                String.format("%.2f", (double) mutatedIndividuals / generation.size() * 100),
                totalMutations);
    }
}