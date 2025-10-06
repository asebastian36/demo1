package com.example.demo.controller;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.BinaryConverterService;
import com.example.demo.visualization.ChartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String showResults(@RequestParam(defaultValue = "1") int currentGeneration,
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

            // ✅ GENERAR GRÁFICA SOLO CUANDO SE CARGA LA VISTA
            String chartImage = chartService.generateAdaptativeChart(fitnessByGeneration, functionType);

            model.addAttribute("generations", generations);
            model.addAttribute("chartImage", chartImage);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("functionType", functionType);
            model.addAttribute("currentGeneration", currentGeneration);
            model.addAttribute("binaryService", binaryConverterService);

            return "results";

        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar resultados: " + e.getMessage());
            return "error";
        }
    }
}