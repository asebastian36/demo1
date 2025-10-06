package com.example.demo.conversion;

import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BinaryConverterService {

    private static final Logger log = LoggerFactory.getLogger(BinaryConverterService.class);

    public List<Integer> convertBinaryListToIntegers(List<String> binaryNumbers) {
        return binaryNumbers.stream()
                .map(this::convertBinaryToInt)
                .collect(Collectors.toList());
    }

    public int convertBinaryToInt(String binaryString) {
        String clean = binaryString.trim();
        // ❌ ELIMINADO: log.trace("Convirtiendo binario a entero: {}", clean);
        if (!clean.matches("[01]+")) {
            log.warn("Cadena no binaria: {}", clean);
            throw new IllegalArgumentException("Cadena no binaria: " + clean);
        }
        try {
            int value = Integer.parseInt(clean, 2);
            // ❌ ELIMINADO: log.debug("Binario {} → Decimal {}", clean, value);
            return value;
        } catch (NumberFormatException e) {
            log.error("Número binario demasiado grande: {}", clean);
            throw new IllegalArgumentException("Número binario demasiado grande: " + clean);
        }
    }

    public String normalizeBinary(String binary, int length) {
        String clean = binary.trim();
        if (clean.length() > length) {
            String result = clean.substring(clean.length() - length);
            // ❌ Opcional: puedes eliminar este log también si no lo necesitas
            // log.trace("Binario recortado {} → {}", clean, result);
            return result;
        } else if (clean.length() < length) {
            String result = String.format("%" + length + "s", clean).replace(' ', '0');
            // ❌ Opcional: puedes eliminar este log también
            // log.trace("Binario rellenado {} → {}", clean, result);
            return result;
        }
        return clean;
    }

    public List<String> normalizeAllBinaries(List<String> binaries, int L) {
        return binaries.stream()
                .map(bin -> normalizeBinary(bin, L))
                .collect(Collectors.toList());
    }
}