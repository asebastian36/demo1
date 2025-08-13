package com.example.demo.controller;

import com.example.demo.service.RealConverterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RealController {

    private final RealConverterService realService;

    public RealController(RealConverterService realService) {
        this.realService = realService;
    }

    @PostMapping("/normalize")
    public String normalizeValues(
            @RequestParam double xmin,
            @RequestParam double xmax,
            @RequestParam int L,
            @RequestParam("decimals") String decimalsStr,
            Model model) {

        try {
            // Convertir el string de decimales a lista
            List<Integer> decimals = Arrays.stream(decimalsStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<Double> normalizedValues = realService.toReal(decimals, xmin, xmax, L);

            model.addAttribute("decimals", decimals);
            model.addAttribute("normalized", normalizedValues);
            model.addAttribute("params", Map.of(
                    "xmin", xmin,
                    "xmax", xmax,
                    "L", L
            ));
            model.addAttribute("showForm", true); // Para poder volver a mostrar el formulario

            return "results";

        } catch (Exception e) {
            model.addAttribute("error", "Error en normalización: " + e.getMessage());
            return "error";
        }
    }
}