package com.example.demo.dto;

import com.example.demo.dto.AlgorithmParameters;
import com.example.demo.dto.GreaterThanXmin;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GreaterThanXminValidator implements ConstraintValidator<GreaterThanXmin, AlgorithmParameters> {

    @Override
    public boolean isValid(AlgorithmParameters parameters, ConstraintValidatorContext context) {
        if (parameters == null || parameters.getXmin() == null || parameters.getXmax() == null) {
            return true; // Dejar que otras validaciones manejen null
        }
        return parameters.getXmax() > parameters.getXmin();
    }
}