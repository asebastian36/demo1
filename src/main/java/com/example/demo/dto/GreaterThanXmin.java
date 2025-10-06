package com.example.demo.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = GreaterThanXminValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GreaterThanXmin {
    String message() default "xmax debe ser mayor que xmin";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}