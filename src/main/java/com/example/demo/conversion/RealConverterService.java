package com.example.demo.conversion;

import org.slf4j.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealConverterService {

    private static final Logger log = LoggerFactory.getLogger(RealConverterService.class);

    // 🚨 CAMBIO DE List<Integer> a List<Long>
    public List<Double> toReal(List<Long> decimals, double xmin, double xmax, int L) {
        if (xmax <= xmin) {
            throw new IllegalArgumentException("xmax debe ser mayor que xmin");
        }
        if (L <= 0) {
            throw new IllegalArgumentException("L debe ser un entero positivo");
        }

        // L sigue siendo la longitud, pero Math.pow(2, L) se usará con cuidado si L > 52
        double maxDecimalValue = Math.pow(2, L) - 1;
        double range = xmax - xmin;
        double scaleFactor = range / maxDecimalValue;

        log.debug("Conversión binario → real: L={}, xmin={}, xmax={}, maxDecimal={}, scaleFactor={}",
                L, xmin, xmax, maxDecimalValue, String.format("%.6f", scaleFactor));

        // 🚨 CAMBIO DE Integer a Long
        return decimals.stream()
                .map(v -> {
                    double real = xmin + (v * scaleFactor);
                    log.trace("Decimal {} → Real {}", v, String.format("%.6f", real));
                    return real;
                })
                .collect(Collectors.toList());
    }

    // 🚨 CAMBIO DE int a long
    public Double toRealSingle(long decimal, double xmin, double xmax, int L) {
        double maxDecimalValue = Math.pow(2, L) - 1;
        double scaleFactor = (xmax - xmin) / maxDecimalValue;

        // 🚨 USAR el valor long 'decimal'
        double real = xmin + (decimal * scaleFactor);
        log.trace("toRealSingle: {} → {}", decimal, String.format("%.6f", real));
        return real;
    }
}