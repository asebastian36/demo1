package com.example.demo.conversion;

import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BinaryConverterService {

    private static final Logger log = LoggerFactory.getLogger(BinaryConverterService.class);

    public List<Long> convertBinaryListToIntegers(List<String> binaryNumbers) {
        return binaryNumbers.stream()
                .map(this::convertBinaryToInt)
                .collect(Collectors.toList());
    }

    // 🚨 CAMBIO DE int a long
    public long convertBinaryToInt(String binaryString) {
        String clean = binaryString.trim();
        if (!clean.matches("[01]+")) {
            log.warn("Cadena no binaria: {}", clean);
            throw new IllegalArgumentException("Cadena no binaria: " + clean);
        }
        try {
            // 🚨 USAR Long.parseLong para manejar hasta 63 bits (incluyendo 34)
            return Long.parseLong(clean, 2);
        } catch (NumberFormatException e) {
            log.error("Número binario demasiado grande (Long): {}", clean);
            throw new IllegalArgumentException("Número binario demasiado grande (Long): " + clean);
        }
    }

    public String normalizeBinary(String binary, int length) {
        String clean = binary.trim();
        if (clean.length() > length) {
            // ❌ Opcional: puedes eliminar este log también si no lo necesitas
            // log.trace("Binario recortado {} → {}", clean, result);
            return clean.substring(clean.length() - length);
        } else if (clean.length() < length) {
            // ❌ Opcional: puedes eliminar este log también
            // log.trace("Binario rellenado {} → {}", clean, result);
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