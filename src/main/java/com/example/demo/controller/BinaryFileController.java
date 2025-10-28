package com.example.demo.controller;

import com.example.demo.dto.GeneticAlgorithmRequest;
import com.example.demo.execution.model.AlgorithmExecutionContext;
import com.example.demo.execution.model.Individual;
import com.example.demo.execution.service.GeneticAlgorithmCore;
import com.example.demo.io.validation.BinaryFileValidator;
import com.example.demo.storage.ExecutionResultCache;
import com.example.demo.preprocessing.AlgorithmParameterPreprocessor;
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

    private final GeneticAlgorithmCore geneticAlgorithmCore;
    private final ExecutionResultCache resultStorageService;
    private final BinaryFileValidator fileValidationService;
    private final AlgorithmParameterPreprocessor parameterPreprocessor;

    public BinaryFileController(GeneticAlgorithmCore geneticAlgorithmCore, // Inyectar core
                                ExecutionResultCache resultStorageService,
                                BinaryFileValidator fileValidationService,
                                AlgorithmParameterPreprocessor parameterPreprocessor) {
        this.geneticAlgorithmCore = geneticAlgorithmCore;
        this.resultStorageService = resultStorageService;
        this.fileValidationService = fileValidationService;
        this.parameterPreprocessor = parameterPreprocessor;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "index";
    }

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@Valid GeneticAlgorithmRequest params,
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

            String actualPopulationMode = params.getInputType();
            List<String> binaryNumbers;

            if ("file".equals(actualPopulationMode)) {
                binaryNumbers = fileValidationService.validateAndReadBinaries(file, params.getL());
            } else {
                binaryNumbers = null;
            }

            String sessionId = session.getId();
            AlgorithmExecutionContext context = new AlgorithmExecutionContext(params.getNumGenerations());

            new Thread(() -> {
                try {
                    List<List<Individual>> generations = geneticAlgorithmCore.runEvolutionWithStatus(
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
                            actualPopulationMode,
                            sessionId,
                            context,
                            params.getConvergenceThreshold()
                    );

                    // ALMACENAR RESULTADOS EN CACHÉ
                    List<List<Double>> fitnessByGeneration = generations.stream()
                            .map(gen -> gen.stream()
                                    .map(Individual::getAdaptative)
                                    .collect(Collectors.toList()))
                            .toList();

                    resultStorageService.store(sessionId, Map.of(
                            "generations", generations,
                            "fitnessByGeneration", fitnessByGeneration,
                            "functionType", params.getFunctionType(),
                            "xmin", params.getXmin(),
                            "xmax", params.getXmax(),
                            "L", params.getL()
                    ));

                    context.markCompleted();

                } catch (Exception e) {
                    context.markCompleted();
                    e.printStackTrace();
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