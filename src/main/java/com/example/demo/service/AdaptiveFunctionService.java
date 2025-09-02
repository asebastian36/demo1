package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdaptiveFunctionService {

    public List<Double> toAdaptive(List<Double> realValues) {
        return realValues.stream()
                .map(x -> x * x + 2 * x + 5)
                .collect(Collectors.toList());
    }

    public double toAdaptiveSingle(double x) {
        return x * x + 2 * x + 5;
    }
}