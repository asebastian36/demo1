package com.example.demo.controller;

import com.example.demo.dto.AlgorithmParameters;
import com.example.demo.entities.Individual;
import com.example.demo.genetic.algorithm.ExecutionContext;
import com.example.demo.genetic.algorithm.GeneticAlgorithmService;
import com.example.demo.storage.ResultStorageService;
import com.example.demo.validation.FileValidationService;
import com.example.demo.preprocessing.ParameterPreprocessor;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BinaryFileController {

    private final GeneticAlgorithmService geneticAlgorithmService;
    private final ResultStorageService resultStorageService;
    private final FileValidationService fileValidationService;
    private final ParameterPreprocessor parameterPreprocessor;

    public BinaryFileController(GeneticAlgorithmService geneticAlgorithmService,
                                ResultStorageService resultStorageService,
                                FileValidationService fileValidationService,
                                ParameterPreprocessor parameterPreprocessor) {
        this.geneticAlgorithmService = geneticAlgorithmService;
        this.resultStorageService = resultStorageService;
        this.fileValidationService = fileValidationService;
        this.parameterPreprocessor = parameterPreprocessor;
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
            parameterPreprocessor.preprocess(params);

            List<String> binaryNumbers = null;
            Integer L_for_GA = params.getL();
            String functionType = params.getFunctionType();

            if ("file".equals(params.getMode())) {
                binaryNumbers = fileValidationService.validateAndReadBinaries(file, L_for_GA);
            }

            String sessionId = session.getId();
            ExecutionContext context = new ExecutionContext(params.getNumGenerations());

            List<String> finalBinaryNumbers = binaryNumbers;

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
                            context,
                            params.getConvergenceThreshold()
                    );

                    List<List<Double>> fitnessByGeneration = generations.stream()
                            .map(gen -> gen.stream()
                                    .map(Individual::getAdaptative)
                                    .collect(Collectors.toList()))
                            .toList();

                    resultStorageService.store(sessionId, Map.of(
                            "generations", generations,
                            "fitnessByGeneration", fitnessByGeneration,
                            "functionType", functionType,
                            "xmin", params.getXmin(),
                            "xmax", params.getXmax(),
                            "L", L_for_GA
                    ));

                    context.markCompleted();

                } catch (Exception e) {
                    context.markCompleted();
                }
            }).start();

            resultStorageService.store(sessionId + "_context", context);

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