package com.example.demo.genetic.operators;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component("simple")
public class SimpleMutation implements MutationStrategy {

    private final Random random = new Random();

    @Override
    public String mutate(String binary, double mutationRate, int L) {
        StringBuilder sb = new StringBuilder(binary);
        boolean mutated = false;

        for (int i = 0; i < sb.length(); i++) {
            if (random.nextDouble() < mutationRate) {
                char bit = sb.charAt(i);
                char newBit = (bit == '0') ? '1' : '0';
                sb.setCharAt(i, newBit);
                mutated = true;
            }
        }

        return sb.toString();
    }

    @Override
    public String getName() {
        return "Mutación Simple (bit flip)";
    }
}