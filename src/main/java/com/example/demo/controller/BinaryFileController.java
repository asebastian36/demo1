package com.example.demo.controller;

import com.example.demo.dto.AlgorithmParameters;
import com.example.demo.genetic.algorithm.ExecutionContext;
import com.example.demo.service.AsyncExecutionService;
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

@Controller
public class BinaryFileController {

    private final AsyncExecutionService asyncExecutionService;
    private final ResultStorageService resultStorageService;
    private final FileValidationService fileValidationService;
    private final ParameterPreprocessor parameterPreprocessor;

    public BinaryFileController(AsyncExecutionService asyncExecutionService,
                                ResultStorageService resultStorageService,
                                FileValidationService fileValidationService,
                                ParameterPreprocessor parameterPreprocessor) {
        this.asyncExecutionService = asyncExecutionService;
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
            Integer finalL = params.getL();
            String functionType = params.getFunctionType();

            if ("file".equals(params.getMode())) {
                binaryNumbers = fileValidationService.validateAndReadBinaries(file, finalL);
            }

            String sessionId = session.getId();
            ExecutionContext context = new ExecutionContext(params.getNumGenerations());

            asyncExecutionService.executeGeneticAlgorithm(
                    binaryNumbers,
                    params.getXmin(),
                    params.getXmax(),
                    finalL,
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