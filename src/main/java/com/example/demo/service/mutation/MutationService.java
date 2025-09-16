package com.example.demo.service.mutation;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MutationService {

    private static final Logger log = LoggerFactory.getLogger(MutationService.class);
    private final Random random = new Random();

    // Mutación por BIT, no por individuo
    public String mutateString(String binary, double mutationRatePerBit) {
        StringBuilder sb = new StringBuilder(binary);
        boolean mutated = false;

        for (int i = 0; i < sb.length(); i++) {
            if (random.nextDouble() < mutationRatePerBit) {
                char bit = sb.charAt(i);
                char newBit = (bit == '0') ? '1' : '0';
                sb.setCharAt(i, newBit);
                mutated = true;
            }
        }

        if (mutated) {
            log.trace("Mutación aplicada: {} → {}", binary, sb.toString());
        }

        return sb.toString();
    }

    public void applyToGeneration(List<Individual> generation, double mutationRatePerBit, int L) {
        log.info("Aplicando mutación por bit a {} individuos (tasa por bit={}%)",
                generation.size(), mutationRatePerBit * 100);
        int mutationsApplied = 0;

        for (int i = 0; i < generation.size(); i++) {
            Individual original = generation.get(i);
            String mutatedBinary = mutateString(original.getBinary(), mutationRatePerBit);

            if (!original.getBinary().equals(mutatedBinary)) {
                // Recalcular real y adaptativo
                int decimal = binaryConverterService.convertBinaryToInt(mutatedBinary);
                double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                double adaptative = adaptiveFunctionService.toAdaptiveSingle(real);

                Individual mutated = new Individual(mutatedBinary, real, adaptative, original.getGeneration());
                generation.set(i, mutated);
                mutationsApplied++;
            }
        }

        log.info("Mutación finalizada: {} de {} individuos modificados", mutationsApplied, generation.size());
    }

    // Inyectar dependencias que faltaban
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;
    private double xmin, xmax; // Necesitamos estos para recalcular

    public MutationService(BinaryConverterService binaryConverterService,
                           RealConverterService realConverterService,
                           AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
    }

    // Método para setear xmin/xmax antes de mutar (lo llamaremos desde GeneticAlgorithmService)
    public void setBounds(double xmin, double xmax) {
        this.xmin = xmin;
        this.xmax = xmax;
    }
}