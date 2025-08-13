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

        double denominator = Math.pow(2, L) - 1;
        double range = xmax - xmin;

        return decimals.stream()
                .map(v -> xmin + (v * (range / denominator)))
                .collect(Collectors.toList());
    }
}
