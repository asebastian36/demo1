package com.example.demo.preprocessing;

import com.example.demo.dto.GeneticAlgorithmRequest;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmParameterPreprocessor {

    public void preprocess(GeneticAlgorithmRequest params) {
        if ("credit".equals(params.getFunctionType())) {
            params.setL(34);
            // Opcional: ajustar xmin/xmax si son irrelevantes para crédito
            // params.setXmin(0.0);
            // params.setXmax(1.0);
        }
    }
}