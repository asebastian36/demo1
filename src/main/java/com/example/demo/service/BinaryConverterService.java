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
        String clean = binaryString.trim();
        if (!clean.matches("[01]+")) {
            throw new IllegalArgumentException("Cadena no binaria: " + clean);
        }
        try {
            return Integer.parseInt(clean, 2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número binario demasiado grande: " + clean);
        }
    }

    public String normalizeBinary(String binary, int length) {
        String clean = binary.trim();
        if (clean.length() > length) {
            return clean.substring(clean.length() - length);
        } else if (clean.length() < length) {
            return String.format("%" + length + "s", clean).replace(' ', '0');
        }
        return clean;
    }

    public List<String> normalizeAllBinaries(List<String> binaries, int L) {
        return binaries.stream()
                .map(bin -> normalizeBinary(bin, L))
                .collect(Collectors.toList());
    }
}