package com.example.demo.controller;

import com.example.demo.dto.AlgorithmParameters;
import com.example.demo.entities.Individual;
import com.example.demo.genetic.algorithm.ExecutionStatus;
import com.example.demo.genetic.algorithm.GeneticAlgorithmService;
import com.example.demo.conversion.BinaryConverterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ExecutionStatus executionStatus;

    public BinaryFileController(GeneticAlgorithmService geneticAlgorithmService,
                                BinaryConverterService binaryConverterService,
                                ExecutionStatus executionStatus) {
        this.geneticAlgorithmService = geneticAlgorithmService;
        this.binaryConverterService = binaryConverterService;
        this.executionStatus = executionStatus;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "index";
    }

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@Valid AlgorithmParameters params,
                                   BindingResult bindingResult,
                                   @RequestParam(required = false) MultipartFile file,
                                   HttpSession session,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder("Errores de validación:");
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(" ").append(error.getDefaultMessage()));
            model.addAttribute("error", errors.toString());
            return "error";
        }

        try {
            List<String> binaryNumbers = null;
            Integer finalL = params.getL();
            String functionType = params.getFunctionType();

            // FORZAR L=34 para la función de crédito
            if ("credit".equals(functionType)) {
                finalL = 34;
            }

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

                binaryNumbers = binaryConverterService.normalizeAllBinaries(binaryNumbers, finalL);
            }

            // ✅ INICIAR EJECUCIÓN EN HILO SEPARADO
            String sessionId = session.getId();
            executionStatus.startExecution(sessionId, params.getNumGenerations());

            List<String> finalBinaryNumbers = binaryNumbers;
            Integer L_for_GA = finalL;

            new Thread(() -> {
                try {
                    List<List<Individual>> generations = geneticAlgorithmService.runEvolutionWithStatus(
                            finalBinaryNumbers,
                            params.getXmin(),
                            params.getXmax(),
                            L_for_GA,
                            functionType,
                            params.getSelectionType(),
                            params.getCrossoverType(),
                            params.getMutationType(),
                            params.getPopulationSize(),
                            params.getNumGenerations(),
                            params.getMutationRate(),
                            params.getCrossoverRate(),
                            params.getMode(),
                            sessionId,
                            executionStatus,
                            // 🚨 PASAR EL NUEVO PARÁMETRO
                            params.getConvergenceThreshold()
                    );

                    List<List<Double>> fitnessByGeneration = generations.stream()
                            .map(gen -> gen.stream()
                                    .map(Individual::getAdaptative)
                                    .collect(Collectors.toList()))
                            .collect(Collectors.toList());

                    session.setAttribute("generations", generations);
                    session.setAttribute("fitnessByGeneration", fitnessByGeneration);
                    session.setAttribute("functionType", functionType);
                    session.setAttribute("xmin", params.getXmin());
                    session.setAttribute("xmax", params.getXmax());
                    session.setAttribute("L", L_for_GA);

                    executionStatus.markCompleted(sessionId);

                } catch (Exception e) {
                    executionStatus.markCompleted(sessionId);
                }
            }).start();

            return "redirect:/loading";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Error de validación: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar: " + e.getMessage());
            return "error";
        }
    }
}