package com.example.demo.controller;

import com.example.demo.execution.model.Individual;
import com.example.demo.function.credit.CreditRiskFitnessFunction;
import com.example.demo.io.conversion.BinaryToDecimalConverter;
import com.example.demo.visualization.FitnessChartGenerator;
import com.example.demo.storage.ExecutionResultCache;
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
    private final BinaryToDecimalConverter binaryConverter;
    private final ExecutionResultCache resultCache;

    public ResultsController(FitnessChartGenerator chartGenerator,
                             BinaryToDecimalConverter binaryConverter,
                             ExecutionResultCache resultCache) {
        this.chartGenerator = chartGenerator;
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

            if ("credit".equals(functionType)) {
                List<Individual> finalGeneration = generations.getLast();
                List<Individual> top10 = finalGeneration.stream()
                        .limit(10)
                        .toList();

                List<Map<String, Object>> top10Interpretations = top10.stream()
                        .map(ind -> CreditRiskFitnessFunction.getInterpretationDetails(ind.getBinary(), binaryConverter))
                        .collect(Collectors.toList());

                model.addAttribute("top10Interpretations", top10Interpretations);
                model.addAttribute("isCreditFunction", true);
                model.addAttribute("bestIndividual", finalGeneration.getFirst());
            } else {
                model.addAttribute("isCreditFunction", false);
                model.addAttribute("currentGenIndividuals", generations.get(generationIndex));
                model.addAttribute("totalGenerations", totalGenerations);
            }

            // 🚨 Generar gráfica al renderizar
            String chartImage = chartGenerator.generateAdaptativeChart(fitnessByGeneration, functionType);
            model.addAttribute("chartImage", chartImage);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("functionType", functionType);
            model.addAttribute("currentGeneration", currentGeneration);
            // 👇 Nombre coherente con Thymeleaf: binaryService
            model.addAttribute("binaryService", binaryConverter);

            return "results";

        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar resultados: " + e.getMessage());
            return "error";
        }
    }
}