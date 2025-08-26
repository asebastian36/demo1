package com.example.demo.service;

import com.example.demo.entities.Individual;
import com.example.demo.utils.IntPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoopConverterService {

    @Autowired
    private CrossConverterService crossConverterService;

    @Autowired
    private BinaryConverterService binaryService;

    @Autowired
    private RealConverterService realService;

    @Autowired
    private AdaptitiveConverterService adaptitiveService;

    public List<List<Individual>> generateGenerations(List<String> binaryNumbers, double xmin, double xmax, int L) {
        List<List<Individual>> generations = new ArrayList<>();

        // Normalizar todos los binarios a longitud L
        List<String> currentBinaries = binaryService.normalizeAllBinaries(binaryNumbers, L);

        System.out.println("=== INICIO DEL ALGORITMO GENÉTICO ===");
        System.out.println("Binarios iniciales normalizados: " + currentBinaries);

        // GENERACIÓN 1
        System.out.println("\n=== GENERACIÓN 1 ===");
        List<Individual> generation1 = createAndOrderGeneration(currentBinaries, xmin, xmax, L, 0);
        generations.add(generation1);

        // Obtener binarios ordenados por adaptativo (descendente)
        List<String> orderedBinariesGen1 = generation1.stream()
                .map(Individual::getBinary)
                .collect(Collectors.toList());
        System.out.println("Binarios Gen 1 ORDENADOS: " + orderedBinariesGen1);

        // GENERACIÓN 2
        System.out.println("\n=== GENERACIÓN 2 ===");
        List<IntPair> crucesGen2 = getCrucePairs(0);
        System.out.println("Cruces originales Gen 1→2: " + crucesGen2);

        // Mapear cruces al orden actual (índices ordenados por adaptativo)
        List<IntPair> mappedCrucesGen2 = mapCrucePairs(crucesGen2, orderedBinariesGen1);
        System.out.println("Cruces mapeados Gen 1→2: " + mappedCrucesGen2);

        List<String> binariosGen2 = crossConverterService.cruceGeneracion(
                new ArrayList<>(orderedBinariesGen1), mappedCrucesGen2, xmin, xmax, L
        );

        List<Individual> generation2 = createAndOrderGeneration(binariosGen2, xmin, xmax, L, 1);
        generations.add(generation2);

        // Obtener binarios ordenados de Gen 2
        List<String> orderedBinariesGen2 = generation2.stream()
                .map(Individual::getBinary)
                .collect(Collectors.toList());
        System.out.println("Binarios Gen 2 ORDENADOS: " + orderedBinariesGen2);

        // GENERACIÓN 3
        System.out.println("\n=== GENERACIÓN 3 ===");
        List<IntPair> crucesGen3 = getCrucePairs(1);
        System.out.println("Cruces originales Gen 2→3: " + crucesGen3);

        // Mapear cruces al orden actual de Gen 2
        List<IntPair> mappedCrucesGen3 = mapCrucePairs(crucesGen3, orderedBinariesGen2);        System.out.println("Cruces mapeados Gen 2→3: " + mappedCrucesGen3);

        List<String> binariosGen3 = crossConverterService.cruceGeneracion(
                new ArrayList<>(orderedBinariesGen2), mappedCrucesGen3, xmin, xmax, L
        );

        List<Individual> generation3 = createAndOrderGeneration(binariosGen3, xmin, xmax, L, 2);
        generations.add(generation3);

        System.out.println("=== FIN DEL ALGORITMO ===");
        return generations;
    }

    private List<Individual> createAndOrderGeneration(List<String> binaryNumbers, double xmin, double xmax, int L, int generationNumber) {
        System.out.println("Creando generación " + (generationNumber + 1) + " con binarios: " + binaryNumbers);

        List<Individual> individuals = new ArrayList<>();
        List<Integer> decimalNumbers = binaryService.convertBinaryListToIntegers(binaryNumbers);
        List<Double> realNumbers = realService.toReal(decimalNumbers, xmin, xmax, L);
        List<Double> adaptativeNumbers = adaptitiveService.toAdaptive(realNumbers);

        for (int i = 0; i < binaryNumbers.size(); i++) {
            Individual individual = new Individual(
                    binaryNumbers.get(i),
                    realNumbers.get(i),
                    adaptativeNumbers.get(i),
                    generationNumber
            );
            individuals.add(individual);
        }

        // Ordenar por valor adaptativo descendente
        // Más explícito y legible
        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());

        System.out.println("Generación " + (generationNumber + 1) + " ordenada por adaptativo descendente");
        return individuals;
    }

    /**
     * Mapea los índices de cruce del orden original al orden actual (ordenado por adaptativo)
     */
    private List<IntPair> mapCrucePairs(List<IntPair> originalCruces, List<String> orderedBinaries) {
        List<IntPair> mappedCruces = new ArrayList<>();

        for (IntPair par : originalCruces) {
            int originalIndex1 = par.first();
            int originalIndex2 = par.second();

            // Los índices ya están en el orden correcto (ordenado por adaptativo)
            // Solo necesitamos asegurar que estén dentro del rango
            if (originalIndex1 <= orderedBinaries.size() && originalIndex2 <= orderedBinaries.size()) {
                mappedCruces.add(new IntPair(originalIndex1, originalIndex2));
            }
        }

        return mappedCruces;
    }
    private List<IntPair> getCrucePairs(int listaIndex) {
        if (listaIndex == 0) {
            return Arrays.asList(
                    new IntPair(4, 14), new IntPair(3, 7), new IntPair(8, 9),
                    new IntPair(15, 1), new IntPair(13, 9), new IntPair(7, 8),
                    new IntPair(9, 10), new IntPair(5, 9), new IntPair(15, 7),
                    new IntPair(9, 1), new IntPair(3, 4)
            );
        } else {
            return Arrays.asList(
                    new IntPair(15, 3), new IntPair(8, 7), new IntPair(2, 9),
                    new IntPair(6, 5), new IntPair(14, 7), new IntPair(3, 9),
                    new IntPair(13, 4), new IntPair(9, 2), new IntPair(15, 9),
                    new IntPair(7, 3), new IntPair(8, 12)
            );
        }
    }
}