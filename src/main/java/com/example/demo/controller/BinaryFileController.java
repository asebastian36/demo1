package com.example.demo.controller;

import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Controller
public class BinaryFileController {

    private final BinaryConverterService binaryService;
    private final RealConverterService realService;
    private final AdaptitiveConverterService adaptitiveService;

    public BinaryFileController(BinaryConverterService binaryService, RealConverterService realService, AdaptitiveConverterService adaptitiveService) {
        this.binaryService = binaryService;
        this.realService = realService;
        this.adaptitiveService = adaptitiveService;
    }

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

            // Conversión a decimales
            List<Integer> decimalNumbers = binaryService.convertBinaryListToIntegers(binaryNumbers);

            //  paso a reales
            List<Double> realNumbers = realService.toReal(decimalNumbers, xmin, xmax, L);

            //  paso a adaptitivos
            List<Double> adaptatives = adaptitiveService.toAdaptive(realNumbers);

            List<Double> listaOrdenada = adaptatives.stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            // Agregar resultados al modelo
            model.addAttribute("xmin", xmin);
            model.addAttribute("xmax", xmax);
            model.addAttribute("L", L);
            model.addAttribute("binaries", binaryNumbers);
            model.addAttribute("decimals", decimalNumbers);
            model.addAttribute("reals", realNumbers);
            model.addAttribute("adaptatives", adaptatives);
            model.addAttribute("listaOrdenada", listaOrdenada);
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