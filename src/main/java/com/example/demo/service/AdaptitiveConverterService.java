package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdaptitiveConverterService {

    public List<Double> toAdaptive(List<Double> realNumbers) {
        return realNumbers.stream()
                .map(x -> x * x + 2 * x + 5)  // f(x) = x² + 2x + 5
                .collect(Collectors.toList());
    }

    // En AdaptitiveConverterService:
    public Double toAdaptiveSingle(double realValue) {
        return realValue * realValue + 2 * realValue + 5;
    }
}