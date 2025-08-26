package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BinaryConverterService {

    public List<Integer> convertBinaryListToIntegers(List<String> binaryNumbers) {
        return binaryNumbers.stream()
                .map(this::convertBinaryToInt)
                .collect(Collectors.toList());
    }

    public int convertBinaryToInt(String binaryString) {
        String cleanString = binaryString.trim();

        if (!cleanString.matches("[01]+")) {
            throw new IllegalArgumentException("Cadena no binaria: " + cleanString);
        }

        try {
            return Integer.parseInt(cleanString, 2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número binario demasiado grande: " + cleanString);
        }
    }

    // Método para normalizar binarios a una longitud específica
    public String normalizeBinary(String binary, int length) {
        if (binary.length() > length) {
            // Si es más largo, tomar los últimos 'length' bits
            return binary.substring(binary.length() - length);
        } else if (binary.length() < length) {
            // Si es más corto, rellenar con ceros a la izquierda
            return String.format("%" + length + "s", binary).replace(' ', '0');
        }
        return binary;
    }

    // Método para normalizar una lista de binarios
    public List<String> normalizeAllBinaries(List<String> binaries, int L) {
        return binaries.stream()
                .map(bin -> normalizeBinary(bin, L))
                .collect(Collectors.toList());
    }
}