package com.example.demo.genetic.operators;

import org.slf4j.*;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component("swap")
public class SwapMutation implements MutationStrategy {

    private static final Logger log = LoggerFactory.getLogger(SwapMutation.class);


    private final Random random = new Random();

    @Override
    public String mutate(String binary, double mutationRate, int L) {
        // Verificar si se aplica mutación a este individuo
        if (random.nextDouble() >= mutationRate) {
            return binary;
        }

        // Verificar que el cromosoma tenga al menos 2 bits
        if (binary.length() < 2) {
            return binary;
        }

        // Seleccionar dos posiciones aleatorias diferentes
        int pos1 = random.nextInt(binary.length());
        int pos2 = random.nextInt(binary.length());

        // Asegurar que sean posiciones diferentes
        while (pos1 == pos2) {
            pos2 = random.nextInt(binary.length());
        }

        // Convertir a array de caracteres para manipulación
        char[] chars = binary.toCharArray();

        // Intercambiar los bits en las posiciones seleccionadas
        char temp = chars[pos1];
        chars[pos1] = chars[pos2];
        chars[pos2] = temp;

        String mutated = new String(chars);

        // Log de depuración (opcional)
        if (!binary.equals(mutated)) {
            log.trace("  🔄 Mutación por intercambio: {} → {} (posiciones {} y {})",
                    binary, mutated, pos1, pos2);
        }

        return mutated;
    }

    @Override
    public String getName() {
        return "Mutación por Intercambio";
    }
}
