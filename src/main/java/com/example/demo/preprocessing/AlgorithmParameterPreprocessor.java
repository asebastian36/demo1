// src/main/java/com/example/demo/preprocessing/AlgorithmParameterPreprocessor.java
package com.example.demo.preprocessing;

import com.example.demo.dto.GeneticAlgorithmRequest;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmParameterPreprocessor {

    public void preprocess(GeneticAlgorithmRequest params) {
        if ("credit".equals(params.getFunctionType())) {
            params.setL(34);
        } else if ("consumo".equals(params.getFunctionType())) {
            params.setL(30);
        }
        // Para funciones matemáticas, L se mantiene como está
    }
}