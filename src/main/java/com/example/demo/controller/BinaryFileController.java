package com.example.demo.controller;

import com.example.demo.entities.Individual;
import com.example.demo.service.algorithm.GeneticAlgorithmService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.visualization.ChartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class BinaryFileController {

    private final GeneticAlgorithmService geneticAlgorithmService;
    private final BinaryConverterService binaryConverterService;
    private final ChartService chartService;

    public BinaryFileController(GeneticAlgorithmService geneticAlgorithmService,
                                BinaryConverterService binaryConverterService,
                                ChartService chartService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
        this.binaryConverterService = binaryConverterService;
        this.chartService = chartService;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "index";
    }

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@RequestParam("mode") String mode,
                                   @RequestParam(required = false) MultipartFile file,
                                   @RequestParam double xmin,
                                   @RequestParam double xmax,
                                   @RequestParam int L,
                                   @RequestParam(defaultValue = "function5") String functionType,
                                   @RequestParam(defaultValue = "roulette") String selectionType,
                                   @RequestParam(defaultValue = "single") String crossoverType,
                                   @RequestParam(defaultValue = "simple") String mutationType,
                                   @RequestParam(required = false, defaultValue = "4200") int populationSize,
                                   @RequestParam(required = false, defaultValue = "30") int numGenerations,
                                   @RequestParam(required = false, defaultValue = "0.001") double mutationRate,
                                   @RequestParam(required = false, defaultValue = "0.8") double crossoverRate,
                                   HttpSession session,
                                   Model model) {
        try {
            List<String> binaryNumbers = null;

            if ("file".equals(mode)) {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("Debe seleccionar un archivo .txt");
                }
                if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".txt")) {
                    throw new IllegalArgumentException("Solo se permiten archivos .txt");
                }

                binaryNumbers = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            binaryNumbers.add(line.trim());
                        }
                    }
                }

                if (binaryNumbers.isEmpty()) {
                    throw new IllegalArgumentException("El archivo no contiene números binarios");
                }

                binaryNumbers = binaryConverterService.normalizeAllBinaries(binaryNumbers, L);
            }

            List<List<Individual>> generations = geneticAlgorithmService.runEvolution(
                    binaryNumbers,
                    xmin, xmax, L,
                    functionType,
                    selectionType, crossoverType, mutationType,
                    populationSize, numGenerations, mutationRate, crossoverRate,
                    mode
            );

            List<List<Double>> fitnessByGeneration = generations.stream()
                    .map(gen -> gen.stream()
                            .map(Individual::getAdaptative)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            String chartImage = chartService.generateAdaptativeChart(fitnessByGeneration, functionType);

            // Guardar en sesión para acceso posterior
            session.setAttribute("generations", generations);
            session.setAttribute("chartImage", chartImage);
            session.setAttribute("xmin", xmin);
            session.setAttribute("xmax", xmax);
            session.setAttribute("L", L);
            session.setAttribute("functionType", functionType);

            // Redirigir a la primera generación
            return "redirect:/results?xmin=" + xmin + "&xmax=" + xmax + "&L=" + L + "&currentGeneration=1";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Error de validación: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar: " + e.getMessage());
            return "error";
        }
    }
}