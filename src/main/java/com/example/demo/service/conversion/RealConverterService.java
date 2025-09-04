package com.example.demo.service.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealConverterService {

    private static final Logger log = LoggerFactory.getLogger(RealConverterService.class);

    public List<Double> toReal(List<Integer> decimals, double xmin, double xmax, int L) {
        if (xmax <= xmin) {
            throw new IllegalArgumentException("xmax debe ser mayor que xmin");
        }
        if (L <= 0) {
            throw new IllegalArgumentException("L debe ser un entero positivo");
        }

        double maxDecimalValue = Math.pow(2, L) - 1;
        double range = xmax - xmin;
        double scaleFactor = range / maxDecimalValue;

        log.debug("Conversión binario → real: L={}, xmin={}, xmax={}, maxDecimal={}, scaleFactor={}",
                L, xmin, xmax, maxDecimalValue, String.format("%.6f", scaleFactor));

        return decimals.stream()
                .map(v -> {
                    double real = xmin + (v * scaleFactor);
                    log.trace("Decimal {} → Real {}", v, String.format("%.6f", real));
                    return real;
                })
                .collect(Collectors.toList());
    }

    public Double toRealSingle(int decimal, double xmin, double xmax, int L) {
        double maxDecimalValue = Math.pow(2, L) - 1;
        double scaleFactor = (xmax - xmin) / maxDecimalValue;
        double real = xmin + (decimal * scaleFactor);
        log.trace("toRealSingle: {} → {}", decimal, String.format("%.6f", real));
        return real;
    }
}