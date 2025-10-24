package com.example.demo.controller;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.ChromosomeBasedFitnessFunction;
import com.example.demo.function.FitnessFunction;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.io.conversion.FitnessEvaluator;
import com.example.demo.storage.ExecutionResultCache;
import com.example.demo.visualization.FitnessChartGenerator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ResultsController {

    private final FitnessChartGenerator chartGenerator;
    private final FitnessEvaluator fitnessEvaluator; // 👈 Necesario para getFunction()
    private final BinaryToDecimalConverter binaryConverter;
    private final ExecutionResultCache resultCache;

    public ResultsController(FitnessChartGenerator chartGenerator,
                             FitnessEvaluator fitnessEvaluator, // 👈 Inyectado
                             BinaryToDecimalConverter binaryConverter,
                             ExecutionResultCache resultCache) {
        this.chartGenerator = chartGenerator;
        this.fitnessEvaluator = fitnessEvaluator;
        this.binaryConverter = binaryConverter;
        this.resultCache = resultCache;
    }

    @GetMapping("/results")
    public String showResults(@RequestParam(defaultValue = "1") int currentGeneration,
                              HttpSession session,
                              Model model) {
        try {
            String sessionId = session.getId();
            @SuppressWarnings("unchecked")
            Map<String, Object> results = resultCache.get(sessionId, Map.class);

            if (results == null) {
                model.addAttribute("error", "No hay resultados disponibles. Por favor ejecute el algoritmo nuevamente.");
                return "error";
            }

            @SuppressWarnings("unchecked")
            List<List<Individual>> generations = (List<List<Individual>>) results.get("generations");
            @SuppressWarnings("unchecked")
            List<List<Double>> fitnessByGeneration = (List<List<Double>>) results.get("fitnessByGeneration");
            String functionType = (String) results.get("functionType");
            Double xmin = (Double) results.get("xmin");
            Double xmax = (Double) results.get("xmax");
            Integer L = (Integer) results.get("L");

            if (generations == null || fitnessByGeneration == null) {
                model.addAttribute("error", "Resultados incompletos. Ejecute el algoritmo nuevamente.");
                return "error";
            }

            int totalGenerations = generations.size();
            currentGeneration = Math.max(1, Math.min(currentGeneration, totalGenerations));
            int generationIndex = currentGeneration - 1;

            boolean isChromosomeBased = "credit".equals(functionType) || "consumo".equals(functionType);

            if (isChromosomeBased) {
                List<Individual> finalGeneration = generations.getLast();
                List<Individual> top10 = finalGeneration.stream()
                        .limit(10)
                        .toList();

                FitnessFunction function = fitnessEvaluator.getFunction(functionType);
                if (!(function instanceof ChromosomeBasedFitnessFunction chromosomeFunction)) {
                    throw new IllegalStateException("Función no compatible con decodificación de cromosoma");
                }

                List<Map<String, Object>> top10Interpretations = top10.stream()
                        .map(ind -> chromosomeFunction.decodeAndInterpret(ind.getBinary(), binaryConverter))
                        .collect(Collectors.toList());

                model.addAttribute("top10Interpretations", top10Interpretations);
                model.addAttribute("isChromosomeBasedFunction", true);
                model.addAttribute("bestIndividual", finalGeneration.getFirst());
            } else {
                model.addAttribute("isChromosomeBasedFunction", false);
                model.addAttribute("currentGenIndividuals", generations.get(generationIndex));
                model.addAttribute("totalGenerations", totalGenerations);
            }

            String chartImage = chartGenerator.generateAdaptativeChart(fitnessByGeneration, functionType);
            model.addAttribute("chartImage", chartImage);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("functionType", functionType);
            model.addAttribute("currentGeneration", currentGeneration);
            model.addAttribute("binaryService", binaryConverter);

            return "results";

        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar resultados: " + e.getMessage());
            return "error";
        }
    }
}