package com.example.demo.genetic.operators;

import com.example.demo.entities.Individual;
import java.util.List;

/**
 * Estrategia para seleccionar padres en el algoritmo genético.
 */
public interface SelectionStrategy {
    /**
     * Selecciona pares de padres para cruces.
     *
     * @param population población actual
     * @param numPairs número de parejas a seleccionar
     * @return lista de pares de individuos (padres)
     */
    List<Individual[]> selectPairs(List<Individual> population, int numPairs);

    /**
     * Nombre descriptivo para mostrar en UI o logs.
     */
    String getName();
}