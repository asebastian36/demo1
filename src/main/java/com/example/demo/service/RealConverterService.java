package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealConverterService {

    public List<Double> toReal(List<Integer> decimals, double xmin, double xmax, int L) {
        if (xmax <= xmin) {
            throw new IllegalArgumentException("xmax debe ser mayor que xmin");
        }
        if (L <= 0) {
            throw new IllegalArgumentException("L debe ser un entero positivo");
        }

        // Calcular el máximo valor decimal posible
        double maxDecimalValue = Math.pow(2, L) - 1;

        // Calcular el factor de escala
        double range = xmax - xmin;
        double scaleFactor = range / maxDecimalValue;

        return decimals.stream()
                .map(v -> xmin + (v * scaleFactor))
                .collect(Collectors.toList());
    }

    // En RealConverterService:
    public Double toRealSingle(int decimal, double xmin, double xmax, int L) {
        double maxDecimalValue = Math.pow(2, L) - 1;
        double scaleFactor = (xmax - xmin) / maxDecimalValue;
        return xmin + (decimal * scaleFactor);
    }
}