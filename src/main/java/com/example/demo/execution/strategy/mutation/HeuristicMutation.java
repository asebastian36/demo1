package com.example.demo.execution.strategy.mutation;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component("heuristic")
public class HeuristicMutation implements MutationStrategy {

    private final Random random = new Random();

    @Override
    public String mutate(String binary, double mutationRate, int L) {
        if (Math.random() >= mutationRate) {
            return binary;
        }

        char[] chars = binary.toCharArray();
        int pos = random.nextInt(L);

        // Heurística: si el bit actual es '0', cambiar a '1' y viceversa
        chars[pos] = (chars[pos] == '0') ? '1' : '0';

        return new String(chars);
    }

    @Override
    public String getName() {
        return "Mutación Heurística";
    }
}