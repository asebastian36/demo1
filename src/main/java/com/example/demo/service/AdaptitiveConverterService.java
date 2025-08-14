package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdaptitiveConverterService {

    public List<Double> toAdaptive(List<Double> realNumbers) {
        return realNumbers.stream()
                .map(this::calculateWithBigDecimal)
                .collect(Collectors.toList());
    }

    private Double calculateWithBigDecimal(Double x) {
        BigDecimal bigX = BigDecimal.valueOf(x);
        // x² + 2x + 5 con 15 decimales de precisión
        return bigX.multiply(bigX)                         // x²
                .add(new BigDecimal("2").multiply(bigX))   // + 2x
                .add(new BigDecimal("5"))                  // + 5
                .setScale(15, RoundingMode.HALF_UP)        // 15 decimales
                .doubleValue();
    }
}
