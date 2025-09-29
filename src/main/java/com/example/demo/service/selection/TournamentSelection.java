package com.example.demo.service.selection;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("tournament")
public class TournamentSelection implements SelectionStrategy {

    private final Random random = new Random();

    // Servicios necesarios para cálculos reales
    private final BinaryConverterService binaryConverterService;
    private final RealConverterService realConverterService;
    private final AdaptiveFunctionService adaptiveFunctionService;

    // Parámetros configurables
    private Double xmin, xmax;
    private Integer L;
    private String functionType;

    public TournamentSelection(
            BinaryConverterService binaryConverterService,
            RealConverterService realConverterService,
            AdaptiveFunctionService adaptiveFunctionService) {
        this.binaryConverterService = binaryConverterService;
        this.realConverterService = realConverterService;
        this.adaptiveFunctionService = adaptiveFunctionService;
    }

    /**
     * Configura los parámetros necesarios para generar individuos aleatorios reales.
     */
    public void configure(double xmin, double xmax, int L, String functionType) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.L = L;
        this.functionType = functionType;
    }

    @Override
    public List<Individual[]> selectPairs(List<Individual> population, int numPairs) {
        List<Individual[]> pairs = new ArrayList<>();
        int tournamentSize = Math.min(3, population.size());
        boolean isOddPopulation = population.size() % 2 == 1;

        for (int i = 0; i < numPairs; i++) {
            List<Individual> selectionPool = new ArrayList<>(population);

            if (isOddPopulation) {
                // Generar individuo aleatorio REAL con cálculos correctos
                Individual randomIndividual = generateRealRandomIndividual(population.get(0).getGeneration());
                selectionPool.add(randomIndividual);
            }

            Individual parent1 = selectTournament(selectionPool, tournamentSize);
            Individual parent2 = selectTournament(selectionPool, tournamentSize);
            pairs.add(new Individual[]{parent1, parent2});
        }

        return pairs;
    }

    /**
     * Genera un individuo aleatorio REAL con binario, valor real y adaptativo calculados correctamente.
     */
    private Individual generateRealRandomIndividual(int generation) {
        // Validar que los parámetros estén configurados
        if (xmin == null || xmax == null || L == null || functionType == null) {
            throw new IllegalStateException(
                    "TournamentSelection no está configurado. Llama a configure() antes de usar.");
        }

        // 1. Generar binario aleatorio de L bits
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < L; i++) {
            sb.append(random.nextBoolean() ? '1' : '0');
        }
        String randomBinary = sb.toString();

        // 2. Convertir binario a decimal
        int decimal = binaryConverterService.convertBinaryToInt(randomBinary);

        // 3. Convertir decimal a valor real
        double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);

        // 4. Calcular adaptativo usando la función correcta
        double adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);

        return new Individual(randomBinary, real, adaptative, generation);
    }

    private Individual selectTournament(List<Individual> selectionPool, int tournamentSize) {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(selectionPool.get(random.nextInt(selectionPool.size())));
        }

        return tournament.stream()
                .max((i1, i2) -> Double.compare(i1.getAdaptative(), i2.getAdaptative()))
                .orElse(selectionPool.get(0));
    }

    @Override
    public String getName() {
        return "Selección por Torneo";
    }
}