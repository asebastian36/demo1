package com.example.demo.controller;

import com.example.demo.dto.GeneticAlgorithmRequest;
import com.example.demo.execution.elitist.model.CombinationResult;
import com.example.demo.execution.elitist.service.ElitistExecutionOrchestrator;
import com.example.demo.io.validation.BinaryFileValidator;
import com.example.demo.preprocessing.AlgorithmParameterPreprocessor;
import com.example.demo.storage.ElitistResultCache;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collections;
import java.util.List;

@Controller
public class ElitistController {

    private final ElitistExecutionOrchestrator orchestrator;
    private final BinaryFileValidator fileValidator;
    private final AlgorithmParameterPreprocessor preprocessor;
    private final ElitistResultCache elitistResultCache;

    public ElitistController(ElitistExecutionOrchestrator orchestrator,
                             BinaryFileValidator fileValidator,
                             AlgorithmParameterPreprocessor preprocessor,
                             ElitistResultCache elitistResultCache) {
        this.orchestrator = orchestrator;
        this.fileValidator = fileValidator;
        this.preprocessor = preprocessor;
        this.elitistResultCache = elitistResultCache;
    }

    @PostMapping("/executeElitist")
    public String executeElitist(@Valid GeneticAlgorithmRequest request,
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
            preprocessor.preprocess(request);

            // 👇 USAR inputType en lugar de mode
            String actualPopulationMode = request.getInputType();
            List<String> binaries = null;

            if ("file".equals(actualPopulationMode)) {
                binaries = fileValidator.validateAndReadBinaries(file, request.getL());
            } else {
                binaries = Collections.emptyList();
            }

            String sessionId = session.getId();
            final List<String> finalBinaries = binaries;

            new Thread(() -> {
                try {
                    List<CombinationResult> results = orchestrator.executeAllCombinations(
                            sessionId,
                            finalBinaries,
                            request.getXmin(),
                            request.getXmax(),
                            request.getL(),
                            request.getFunctionType(),
                            actualPopulationMode,
                            request.getPopulationSize(),
                            request.getNumGenerations(),
                            request.getMutationRate(),
                            request.getCrossoverRate(),
                            request.getConvergenceThreshold()
                    );
                    elitistResultCache.store(sessionId, results);
                } catch (Exception e) {
                    System.err.println("Error en modo elitista: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

            return "redirect:/loading-elitist?sessionId=" + sessionId;

        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/results/elitist")
    public String showElitistResults(@RequestParam String sessionId, Model model) {
        List<CombinationResult> results = elitistResultCache.get(sessionId);
        if (results == null || results.isEmpty()) {
            model.addAttribute("error", "No hay resultados elitistas disponibles.");
            return "error";
        }
        model.addAttribute("results", results);
        return "results-elitist";
    }
}