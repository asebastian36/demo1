package com.example.demo.genetic.operators;

public interface MutationStrategy {
    /**
     * Aplica mutación a un cromosoma binario.
     *
     * @param binary cadena binaria del individuo
     * @param mutationRate tasa de mutación (0.0 a 1.0)
     * @param L longitud del cromosoma
     * @return cadena binaria mutada
     */
    String mutate(String binary, double mutationRate, int L);

    /**
     * Nombre descriptivo para mostrar en UI o logs.
     */
    String getName();
}