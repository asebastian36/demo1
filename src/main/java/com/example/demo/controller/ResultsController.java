package com.example.demo.controller;

import com.example.demo.entities.Individual;
import com.example.demo.conversion.BinaryConverterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ResultsController {

    private final BinaryConverterService binaryConverterService;

    public ResultsController(BinaryConverterService binaryConverterService) {
        this.binaryConverterService = binaryConverterService;
    }

    @GetMapping("/results")
    public String showResults(@RequestParam double xmin,
                              @RequestParam double xmax,
                              @RequestParam int L,
                              @RequestParam(defaultValue = "1") int currentGeneration,
                              HttpSession session,
                              Model model) {
        try {
            @SuppressWarnings("unchecked")
            List<List<Individual>> generations = (List<List<Individual>>) session.getAttribute("generations");
            String chartImage = (String) session.getAttribute("chartImage");
            String functionType = (String) session.getAttribute("functionType");

            if (generations == null) {
                model.addAttribute("error", "No hay resultados disponibles. Por favor ejecute el algoritmo nuevamente.");
                return "error";
            }

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