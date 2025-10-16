package com.example.demo.controller;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.BinaryConverterService;
import com.example.demo.visualization.ChartService;
import com.example.demo.genetic.function.CreditFitnessFunction;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ResultsController {

    private final ChartService chartService;
    private final BinaryConverterService binaryConverterService;

    public ResultsController(ChartService chartService,
                             BinaryConverterService binaryConverterService) {
        this.chartService = chartService;
        this.binaryConverterService = binaryConverterService;
    }

    @GetMapping("/results")
    public String showResults(@RequestParam(defaultValue = "1") int currentGeneration, // 🚨 Recibir la generación actual
                              HttpSession session,
                              Model model) {
        try {
            @SuppressWarnings("unchecked")
            List<List<Individual>> generations = (List<List<Individual>>) session.getAttribute("generations");
            @SuppressWarnings("unchecked")
            List<List<Double>> fitnessByGeneration = (List<List<Double>>) session.getAttribute("fitnessByGeneration");
            String functionType = (String) session.getAttribute("functionType");
            Double xmin = (Double) session.getAttribute("xmin");
            Double xmax = (Double) session.getAttribute("xmax");
            Integer L = (Integer) session.getAttribute("L");

            if (generations == null || fitnessByGeneration == null) {
                model.addAttribute("error", "No hay resultados disponibles. Por favor ejecute el algoritmo nuevamente.");
                return "error";
            }

            int totalGenerations = generations.size();
            // Asegurar que currentGeneration esté dentro de los límites [1, totalGenerations]
            currentGeneration = Math.max(1, Math.min(currentGeneration, totalGenerations));
            int generationIndex = currentGeneration - 1;

            // --- LÓGICA DE INTERPRETACIÓN PARA CRÉDITO ---
            if ("credit".equals(functionType)) {
                List<Individual> finalGeneration = generations.getLast();

                List<Individual> top10 = finalGeneration.stream()
                        .limit(10)
                        .toList();

                List<Map<String, Object>> top10Interpretations = top10.stream()
                        .map(ind -> CreditFitnessFunction.getInterpretationDetails(ind.getBinary(), binaryConverterService))
                        .collect(Collectors.toList());

                model.addAttribute("top10Interpretations", top10Interpretations);
                model.addAttribute("isCreditFunction", true);
                model.addAttribute("bestIndividual", finalGeneration.getFirst());
            } else {
                model.addAttribute("isCreditFunction", false);

                // 🚨 LÓGICA PARA EL FLUJO F(X): PASAR LA GENERACIÓN ACTUAL
                model.addAttribute("currentGenIndividuals", generations.get(generationIndex));
                model.addAttribute("totalGenerations", totalGenerations);
            }
            // ---------------------------------------------

            // Lógica común
            String chartImage = chartService.generateAdaptativeChart(fitnessByGeneration, functionType);

            model.addAttribute("generations", generations); // Se mantiene para el conteo total
            model.addAttribute("chartImage", chartImage);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("functionType", functionType);
            model.addAttribute("currentGeneration", currentGeneration); // 🚨 Se pasa la generación actual de 1 a N
            model.addAttribute("binaryService", binaryConverterService);

            return "results";

        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar resultados: " + e.getMessage());
            return "error";
        }
    }
}