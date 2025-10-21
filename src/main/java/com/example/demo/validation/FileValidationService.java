package com.example.demo.validation;

import com.example.demo.conversion.BinaryConverterService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FileValidationService {

    private final BinaryConverterService binaryConverterService;

    public FileValidationService(BinaryConverterService binaryConverterService) {
        this.binaryConverterService = binaryConverterService;
    }

    public List<String> validateAndReadBinaries(MultipartFile file, int L) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo .txt");
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if (!originalFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("Solo se permiten archivos .txt");
        }

        List<String> binaryNumbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String cleanLine = line.trim();
                    if (!cleanLine.matches("[01]+")) {
                        throw new IllegalArgumentException("El archivo contiene caracteres no binarios en la línea: " + cleanLine);
                    }
                    binaryNumbers.add(cleanLine);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al leer el archivo: " + e.getMessage());
        }

        if (binaryNumbers.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene números binarios");
        }

        return binaryConverterService.normalizeAllBinaries(binaryNumbers, L);
    }
}