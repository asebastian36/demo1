package com.example.demo.service.algorithm;

import com.example.demo.entities.Individual;
import com.example.demo.service.conversion.*;
import com.example.demo.service.crossover.CrossoverService;
import com.example.demo.service.function.FitnessFunction;
import com.example.demo.service.mutation.MutationService;
import com.example.demo.service.persistence.IndividualService;
import com.example.demo.service.selection.SelectionStrategy;
import com.example.demo.service.population.PopulationSource;
import com.example.demo.service.selection.TournamentSelection;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticAlgorithmService {

    private static final Logger log = LoggerFactory.getLogger(GeneticAlgorithmService.class);

    private final AdaptiveFunctionService adaptiveFunctionService;
    private final CrossoverService crossoverService;
    private final MutationService mutationService;
    private final BinaryConverterService binaryConverterService;
    private final IndividualService individualService;
    private final RealConverterService realConverterService;
    private final Map<String, SelectionStrategy> selectionStrategies;
    private final Map<String, PopulationSource> populationSources;

    public GeneticAlgorithmService(AdaptiveFunctionService adaptiveFunctionService,
                                   RealConverterService realConverterService,
                                   CrossoverService crossoverService,
                                   MutationService mutationService,
                                   BinaryConverterService binaryConverterService,
                                   IndividualService individualService,
                                   Map<String, SelectionStrategy> selectionStrategies,
                                   Map<String, PopulationSource> populationSources) {
        this.adaptiveFunctionService = adaptiveFunctionService;
        this.realConverterService = realConverterService;
        this.crossoverService = crossoverService;
        this.mutationService = mutationService;
        this.binaryConverterService = binaryConverterService;
        this.individualService = individualService;
        this.selectionStrategies = selectionStrategies;
        this.populationSources = populationSources;
    }

    @Transactional
    public List<List<Individual>> runEvolution(
            List<String> fileBinaries,
            double xmin,
            double xmax,
            int L,
            String functionType,
            String selectionType,
            String crossoverType,
            String mutationType,
            int populationSize,
            int numGenerations,
            double mutationRatePerBit,
            double crossoverRate,
            String populationSourceType) {

        Instant start = Instant.now();

        log.info("🚀 INICIANDO ALGORITMO GENÉTICO");
        log.info("   Función: {}", adaptiveFunctionService.getFunction(functionType).getName());
        log.info("   Modo de población: {}", populationSourceType);
        log.info("   Generaciones: {}", numGenerations);
        log.info("   Selección: {}", selectionType);
        log.info("   Cruce: {}", crossoverType);
        log.info("   Mutación: {}", mutationType);
        log.info("   Prob. Cruce: {}%", crossoverRate * 100);
        log.info("   Prob. Mutación: {}%", mutationRatePerBit * 100);
        log.info("   Rango: x ∈ [{}, {}]", xmin, xmax);

        // CONFIGURAR FUENTE DE POBLACIÓN
        PopulationSource populationSource = populationSources.get(populationSourceType);
        if (populationSource == null) {
            throw new IllegalArgumentException("Fuente de población desconocida: " + populationSourceType);
        }

        if ("file".equals(populationSourceType)) {
            if (fileBinaries == null || fileBinaries.isEmpty()) {
                throw new IllegalArgumentException("No se proporcionaron binarios para el modo archivo");
            }
            ((com.example.demo.service.population.FilePopulationSource) populationSource).setBinaries(fileBinaries);
        } else if ("random".equals(populationSourceType)) {
            ((com.example.demo.service.population.RandomPopulationSource) populationSource).setPopulationSize(populationSize);
        }

        List<String> currentBinaries = populationSource.generatePopulation(L);
        log.info("→ Población inicial generada ({}): {} individuos",
                populationSource.getName(), currentBinaries.size());

        mutationService.setBounds(xmin, xmax);

        List<List<Individual>> generations = new ArrayList<>();

        for (int gen = 0; gen < numGenerations; gen++) {
            log.info(" ");
            log.info("════════════════════════════════════════════════");
            log.info("        🎯 GENERACIÓN {} de {}", gen + 1, numGenerations);
            log.info("════════════════════════════════════════════════");

            List<Individual> generation = createOrderedGeneration(currentBinaries, xmin, xmax, L, gen, functionType);
            generations.add(generation);

            if (gen < numGenerations - 1) {
                // 🔑 CALCULAR TAMAÑO ACTUAL DE POBLACIÓN
                int currentPopulationSize = currentBinaries.size();

                // 🔑 CALCULAR NÚMERO DE PAREJAS (REDONDEO HACIA ARRIBA)
                int numPairs = (currentPopulationSize + 1) / 2;

                SelectionStrategy selection = selectionStrategies.get(selectionType);
                if (selection == null) {
                    throw new IllegalArgumentException("Tipo de selección desconocido: " + selectionType);
                }

                // Configurar TournamentSelection si es necesario
                if ("tournament".equals(selectionType) && selection instanceof TournamentSelection) {
                    ((TournamentSelection) selection).configure(xmin, xmax, L, functionType);
                }

                log.info("→ SELECCIÓN: {}", selection.getName());
                List<Individual[]> parentPairs = selection.selectPairs(generation, numPairs);

                log.info("→ CRUCE: Generando hijos con cruce de un punto (probabilidad = {}%)",
                        crossoverRate * 100);
                List<Individual> offspring = new ArrayList<>();
                int crossoverCount = 0;

                for (int i = 0; i < parentPairs.size(); i++) {
                    Individual[] pair = parentPairs.get(i);
                    Individual p1 = pair[0];
                    Individual p2 = pair[1];

                    String bin1 = binaryConverterService.normalizeBinary(p1.getBinary(), L);
                    String bin2 = binaryConverterService.normalizeBinary(p2.getBinary(), L);

                    String[] children;
                    if (Math.random() < crossoverRate) {
                        children = crossoverService.crossoverWithLogging(bin1, bin2, crossoverType, i + 1, L, xmin, xmax, functionType);
                        crossoverCount++;
                    } else {
                        children = new String[]{bin1, bin2};
                    }

                    for (String childBinary : children) {
                        int decimal = binaryConverterService.convertBinaryToInt(childBinary);
                        double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
                        double adaptative = adaptiveFunctionService.toAdaptiveSingle(real, functionType);
                        offspring.add(new Individual(childBinary, real, adaptative, gen + 1));
                    }
                }
                log.info("→ ✅ Cruce completado: {} parejas cruzaron ({}%)", crossoverCount,
                        String.format("%.1f", (double) crossoverCount / parentPairs.size() * 100));

                log.info("→ MUTACIÓN ({}): Aplicando con tasa = {}%", mutationType, mutationRatePerBit * 100);
                mutationService.applyToGenerationWithLogging(offspring, mutationRatePerBit, L, gen + 1, mutationType, functionType);

                // 🔑 ASEGURAR TAMAÑO CONSTANTE DE POBLACIÓN
                if (offspring.size() > currentPopulationSize) {
                    // Recortar al tamaño original
                    offspring = new ArrayList<>(offspring.subList(0, currentPopulationSize));
                } else if (offspring.size() < currentPopulationSize) {
                    // Rellenar con copias del mejor individuo (por seguridad)
                    Individual best = offspring.isEmpty() ? generation.get(0) : offspring.get(0);
                    while (offspring.size() < currentPopulationSize) {
                        offspring.add(new Individual(best.getBinary(), best.getReal(), best.getAdaptative(), gen + 1));
                    }
                }

                currentBinaries = offspring.stream().map(Individual::getBinary).collect(Collectors.toList());
                log.info("→ Población ajustada a {} individuos", currentBinaries.size());
            }
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info(" ");
        log.info("✅✅✅ ALGORITMO FINALIZADO ✅✅✅");
        log.info("⏱️  Tiempo total de ejecución: {} minutos {} segundos",
                duration.toMinutes(), duration.minusMinutes(duration.toMinutes()).getSeconds());

        verifyConvergence(generations.get(generations.size() - 1), functionType);

        return generations;
    }

    private List<Individual> createOrderedGeneration(List<String> binaries, double xmin, double xmax, int L, int generationIndex, String functionType) {
        List<Integer> decimals = binaryConverterService.convertBinaryListToIntegers(binaries);
        List<Double> reals = realConverterService.toReal(decimals, xmin, xmax, L);
        List<Double> fitnessValues = adaptiveFunctionService.toAdaptive(reals, functionType);

        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < binaries.size(); i++) {
            individuals.add(new Individual(binaries.get(i), reals.get(i), fitnessValues.get(i), generationIndex));
        }

        individuals.sort(Comparator.comparingDouble(Individual::getAdaptative).reversed());
        return individuals;
    }

    private void verifyConvergence(List<Individual> finalGeneration, String functionType) {
        FitnessFunction function = adaptiveFunctionService.getFunction(functionType);
        double targetX = function.getTargetX();

        long countConverged = finalGeneration.stream()
                .filter(ind -> Math.abs(Math.abs(ind.getReal()) - targetX) < 0.1)
                .count();

        double percentage = (double) countConverged / finalGeneration.size() * 100;
        log.info(" ");
        log.info("📊 RESULTADO FINAL DE CONVERGENCIA:");
        log.info("   → Individuos en x ≈ ±{}: {} de {}", targetX, countConverged, finalGeneration.size());
        log.info("   → Porcentaje: %.2f%%", percentage);

        if (percentage >= 80) {
            log.info("🎉 ✅ ¡CONVERGENCIA EXITOSA! (≥80% en x ≈ ±{})", targetX);
        } else {
            log.warn("⚠️ ❌ Convergencia insuficiente (<80% en x ≈ ±{})", targetX);
        }
    }
}