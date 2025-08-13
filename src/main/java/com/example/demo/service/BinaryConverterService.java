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
}
