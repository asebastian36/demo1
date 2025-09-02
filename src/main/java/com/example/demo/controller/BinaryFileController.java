package com.example.demo.controller;

import com.example.demo.entities.Individual;
import com.example.demo.service.BinaryConverterService;
import com.example.demo.service.ChartService;
import com.example.demo.service.GeneticAlgorithmService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
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
        return "upload";
    }

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam double xmin,
                                   @RequestParam double xmax,
                                   @RequestParam int L,
                                   Model model) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }
            if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Solo se permiten archivos .txt");
            }

            List<String> binaryNumbers = new ArrayList<>();
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

            List<List<Individual>> generations = geneticAlgorithmService.runEvolution(
                    binaryNumbers, xmin, xmax, L, 3  // 3 generaciones
            );

            List<List<Double>> fitnessByGeneration = generations.stream()
                    .map(gen -> gen.stream()
                            .map(Individual::getAdaptative)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            String chartImage = chartService.generateAdaptativeChart(fitnessByGeneration);

            model.addAttribute("generations", generations);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("binaryService", binaryConverterService);
            model.addAttribute("chartImage", chartImage);

            return "results";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Error de validación: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el archivo: " + e.getMessage());
            return "error";
        }
    }
}