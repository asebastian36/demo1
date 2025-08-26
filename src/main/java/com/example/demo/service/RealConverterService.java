package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealConverterService {

    // Precisión de 15 decimales para cálculos intermedios
    private static final int PRECISION = 15;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public List<Double> toReal(List<Integer> decimals, double xmin, double xmax, int L) {
        if (xmax <= xmin) {
            throw new IllegalArgumentException("xmax debe ser mayor que xmin");
        }
        if (L <= 0) {
            throw new IllegalArgumentException("L debe ser un entero positivo");
        }

        BigDecimal bigXmin = BigDecimal.valueOf(xmin);
        BigDecimal bigXmax = BigDecimal.valueOf(xmax);
        BigDecimal bigRange = bigXmax.subtract(bigXmin);

        // Calcular denominador: (2^L - 1)
        BigDecimal denominator = BigDecimal.valueOf(2)
                .pow(L)  // 2^11 = 2048
                .subtract(BigDecimal.ONE);  // 2047

        return decimals.stream()
                .map(v -> calculateRealValue(v, bigXmin, bigRange, denominator))
                .collect(Collectors.toList());
    }
    private Double calculateRealValue(Integer v, BigDecimal xmin,
                                      BigDecimal range, BigDecimal denominator) {
        BigDecimal bigV = new BigDecimal(v);

        // Fórmula: xmin + (v * (range / denominator))
        return range.divide(denominator, PRECISION, ROUNDING_MODE)  // range / denominator
                .multiply(bigV)                                     // v * (range/denominator)
                .add(xmin)                                          // xmin + ...
                .setScale(PRECISION, ROUNDING_MODE)                 // Redondear a 10 decimales
                .doubleValue();                                     // Convertir a double
    }
}