package com.example.demo.controller;

import com.example.demo.dto.AlgorithmParameters;
import com.example.demo.entities.Individual;
import com.example.demo.service.algorithm.GeneticAlgorithmService;
import com.example.demo.service.conversion.BinaryConverterService;
import com.example.demo.service.visualization.ChartService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;
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
    public String handleFileUpload(@Valid AlgorithmParameters params,
                                   BindingResult bindingResult,
                                   @RequestParam(required = false) MultipartFile file,
                                   Model model) {

        // Validación automática
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder("Errores de validación:");
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(" ").append(error.getDefaultMessage()));
            model.addAttribute("error", errors.toString());
            return "error";
        }

        try {
            List<String> binaryNumbers = null;

            if ("file".equals(params.getMode())) {
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

                binaryNumbers = binaryConverterService.normalizeAllBinaries(binaryNumbers, params.getL());
            }

            // ✅ EJECUTAR ALGORITMO CON LOS PARÁMETROS VALIDADOS
            List<List<Individual>> generations = geneticAlgorithmService.runEvolution(
                    binaryNumbers,
                    params.getXmin(),
                    params.getXmax(),
                    params.getL(),
                    params.getFunctionType(),
                    params.getSelectionType(),
                    params.getCrossoverType(),
                    params.getMutationType(),
                    params.getPopulationSize(),
                    params.getNumGenerations(),
                    params.getMutationRate(),
                    params.getCrossoverRate(),
                    params.getMode()
            );

            // ✅ GENERAR GRÁFICA
            List<List<Double>> fitnessByGeneration = generations.stream()
                    .map(gen -> gen.stream()
                            .map(Individual::getAdaptative)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            String chartImage = chartService.generateAdaptativeChart(fitnessByGeneration, params.getFunctionType());

            // ✅ AGREGAR ATRIBUTOS AL MODELO (incluyendo currentGeneration para la vista)
            model.addAttribute("generations", generations);
            model.addAttribute("chartImage", chartImage);
            model.addAttribute("xmin", params.getXmin());
            model.addAttribute("xmax", params.getXmax());
            model.addAttribute("L", params.getL());
            model.addAttribute("binaryService", binaryConverterService);
            model.addAttribute("currentGeneration", 1);

            return "results";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Error de validación: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar: " + e.getMessage());
            return "error";
        }
    }
}