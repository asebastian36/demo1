package com.example.demo.genetic.operators;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component("inversive")
public class InversiveMutation implements MutationStrategy {

    private final Random random = new Random();

    @Override
    public String mutate(String binary, double mutationRate, int L) {
        if (random.nextDouble() >= mutationRate) {
            return binary; // No muta
        }

        if (binary.length() < 2) {
            return binary;
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

        // Invertir segmento
        String segment = binary.substring(start, end + 1);
        String reversed = new StringBuilder(segment).reverse().toString();

        return binary.substring(0, start) + reversed + binary.substring(end + 1);
    }

    @Override
    public String getName() {
        return "Mutación Inversiva";
    }
}