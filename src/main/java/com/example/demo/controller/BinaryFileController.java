package com.example.demo.controller;

import com.example.demo.service.BinaryConverterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Controller
public class BinaryFileController {

    private final BinaryConverterService binaryService;

    public BinaryFileController(BinaryConverterService binaryService) {
        this.binaryService = binaryService;
    }

    @PostMapping("/uploadTxt")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model) {
        try {
            // Validación básica del archivo
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
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

            // Conversión a decimales
            List<Integer> decimalNumbers = binaryService.convertBinaryListToIntegers(binaryNumbers);

            // Agregar resultados al modelo
            model.addAttribute("decimals", decimalNumbers);
            model.addAttribute("showForm", true); // Mostrar formulario de normalización
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