package com.example.demo.service.mutation;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.*;

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

    // Mutación inversiva
    private String applyInversiveMutation(String binary, double mutationRate) {
        if (Math.random() >= mutationRate) {
            return binary; // No muta
        }

        if (binary.length() < 2) {
            return binary; // No se puede invertir
        }

        // Elegir dos puntos de corte aleatorios
        int start = random.nextInt(binary.length());
        int end = random.nextInt(binary.length());

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        if (start == end) {
            end = Math.min(end + 1, binary.length() - 1);
        }

        // Extraer segmento
        String segment = binary.substring(start, end + 1);
        StringBuilder reversed = new StringBuilder(segment).reverse();

        // Reconstruir cadena
        String mutated = binary.substring(0, start) + reversed.toString() + binary.substring(end + 1);

        log.trace("  🔄 Mutación inversiva: {} → {} (segmento [{}:{}])", binary, mutated, start, end);
        return mutated;
    }

    // Mutación simple (bit flip)
    private String mutateString(String binary, double mutationRatePerBit) {
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
            log.trace("  🧬 Mutación simple aplicada: {} → {}", binary, sb.toString());
        }

        return sb.toString();
    }

    // Método principal que aplica la mutación según el tipo
    public void applyToGenerationWithLogging(List<Individual> generation, double mutationRate, int L, int gen, String mutationType) {
        log.info("→ Iniciando mutación ({}) en generación {} (tasa: {}%)", mutationType, gen, mutationRate * 100);
        int totalMutations = 0;
        int mutatedIndividuals = 0;

        for (int i = 0; i < generation.size(); i++) {
            Individual original = generation.get(i);
            String originalBinary = original.getBinary();
            String mutatedBinary;

            if ("inversive".equals(mutationType)) {
                mutatedBinary = applyInversiveMutation(originalBinary, mutationRate);
            } else {
                mutatedBinary = mutateString(originalBinary, mutationRate);
            }

            if (!originalBinary.equals(mutatedBinary)) {
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

        log.info("→ 🧬 Mutación ({}) finalizada en generación {}: {} individuos mutados ({}%)",
                mutationType, gen,
                mutatedIndividuals,
                String.format("%.2f", (double) mutatedIndividuals / generation.size() * 100));
    }
}