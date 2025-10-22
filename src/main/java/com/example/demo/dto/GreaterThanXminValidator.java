package com.example.demo.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GreaterThanXminValidator implements ConstraintValidator<GreaterThanXmin, GeneticAlgorithmRequest> {

    @Override
    public boolean isValid(GeneticAlgorithmRequest parameters, ConstraintValidatorContext context) {
        if (parameters == null || parameters.getXmin() == null || parameters.getXmax() == null) {
            return true; // Dejar que otras validaciones manejen null
        }
        return parameters.getXmax() > parameters.getXmin();
    }
}