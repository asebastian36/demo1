package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdaptiveFunctionService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveFunctionService.class);

    public List<Double> toAdaptive(List<Double> realValues) {
        log.debug("Aplicando función adaptativa f(x) = x² + 2x + 5 a {} valores", realValues.size());
        return realValues.stream()
                .map(x -> {
                    double result = x * x + 2 * x + 5;
                    log.trace("f({}) = {:.3f}", x, result);
                    return result;
                })
                .collect(Collectors.toList());
    }

    public double toAdaptiveSingle(double x) {
        double result = x * x + 2 * x + 5;
        log.trace("toAdaptiveSingle: f({}) = {:.3f}", x, result);
        return result;
    }
}