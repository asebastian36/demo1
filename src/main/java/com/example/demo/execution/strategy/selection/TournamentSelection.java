package com.example.demo.execution.strategy.selection;

import com.example.demo.execution.model.Individual;
import com.example.demo.genetic.operators.SelectionStrategy;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.io.conversion.DecimalToRealConverter;
import com.example.demo.io.conversion.FitnessEvaluator;
import com.example.demo.function.ChromosomeBasedFitnessFunction; // Importar la interfaz
import org.springframework.stereotype.Component;
import java.util.*;

@Component("tournament")
public class TournamentSelection implements SelectionStrategy {

    private final Random random = new Random();

    // Servicios necesarios para cálculos reales
    private final BinaryToDecimalConverter binaryConverterService;
    private final DecimalToRealConverter realConverterService;
    private final FitnessEvaluator adaptiveFunctionService;

    // Parámetros configurables
    private Double xmin, xmax;
    private Integer L;
    private String functionType;

    public TournamentSelection(
            BinaryToDecimalConverter binaryConverterService,
            DecimalToRealConverter realConverterService,
            FitnessEvaluator adaptiveFunctionService) {
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
                Individual randomIndividual = generateRealRandomIndividual(population.getFirst().getGeneration());
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
     * * **CORRECCIÓN:** Asegura que las funciones basadas en cromosoma usen evaluate(String binary).
     */
    private Individual generateRealRandomIndividual(int generation) {
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

        double real = 0.0;
        double adaptative = 0.0;

        // --- INICIO DE LA CORRECCIÓN ---
        if ("credit".equals(functionType) || "consumo".equals(functionType) || "farmacologia".equals(functionType)) {
            // Si es una función basada en cromosoma, se usa evaluate(String binary) directamente
            // para obtener el fitness adaptativo final.
            if (adaptiveFunctionService.getFunction(functionType) instanceof ChromosomeBasedFitnessFunction function) {
                adaptative = function.evaluate(randomBinary);
                real = 0.0; // El valor real no es relevante para el individuo en este contexto
            } else {
                throw new IllegalStateException("Función de cromosoma mal tipada.");
            }
        } else {
            // Flujo original para funciones matemáticas simples (f(x))
            long decimal = binaryConverterService.convertBinaryToInt(randomBinary);
            real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
            adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
        }
        // --- FIN DE LA CORRECCIÓN ---

        return new Individual(randomBinary, real, adaptative, generation);
    }

    private Individual selectTournament(List<Individual> selectionPool, int tournamentSize) {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(selectionPool.get(random.nextInt(selectionPool.size())));
        }

        return tournament.stream()
                .max(Comparator.comparingDouble(Individual::getAdaptative))
                .orElse(selectionPool.getFirst());
    }

    @Override
    public String getName() {
        return "Selección por Torneo";
    }
}