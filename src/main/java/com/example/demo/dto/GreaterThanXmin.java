package com.example.demo.dto;

import jakarta.validation.*;
import java.lang.annotation.*;

@Constraint(validatedBy = GreaterThanXminValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GreaterThanXmin {
    String message() default "xmax debe ser mayor que xmin";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}