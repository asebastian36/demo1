package com.example.demo.controller;

import com.example.demo.domain.Table;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Controller
public class BinaryFileController {

    @Autowired
    private LoopConverterService loopConverterService;

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam double xmin,
                                   @RequestParam double xmax,
                                   @RequestParam int L,
                                   Model model) {
        try {
            // Validación básica del archivo
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Solo se permiten archivos .txt");
            }

            // Leer el archivo línea por línea
            List<String> binaryNumbers = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        binaryNumbers.add(line.trim());
                    }
                }
            }

            List<Table> tablas = loopConverterService.generateGenerations(binaryNumbers, xmin,  xmax, L);

            // Agregar al modelo
            model.addAttribute("tablas", tablas);
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);


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