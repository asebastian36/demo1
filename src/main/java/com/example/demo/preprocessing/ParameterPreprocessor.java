package com.example.demo.preprocessing;

import com.example.demo.dto.AlgorithmParameters;
import org.springframework.stereotype.Service;

@Service
public class ParameterPreprocessor {

    public void preprocess(AlgorithmParameters params) {
        if ("credit".equals(params.getFunctionType())) {
            params.setL(34);
            // Opcional: ajustar xmin/xmax si son irrelevantes para crédito
            // params.setXmin(0.0);
            // params.setXmax(1.0);
        }
    }
}