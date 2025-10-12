package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.util.List;

@GreaterThanXmin
public class AlgorithmParameters {

    @NotNull(message = "El modo de entrada es requerido")
    private String mode;

    private List<String> fileBinaries;

    @NotNull(message = "xmin es requerido")
    @DecimalMin(value = "-1000.0", message = "xmin debe ser mayor o igual a -1000")
    @DecimalMax(value = "1000.0", message = "xmin debe ser menor o igual a 1000")
    private Double xmin;

    @NotNull(message = "xmax es requerido")
    @DecimalMin(value = "-1000.0", message = "xmax debe ser mayor o igual a -1000")
    @DecimalMax(value = "1000.0", message = "xmax debe ser menor o igual a 1000")
    private Double xmax;

    @NotNull(message = "L es requerido")
    @Min(value = 1, message = "L debe ser al menos 1")
    @Max(value = 32, message = "L no puede ser mayor que 32")
    private Integer L;

    @NotBlank(message = "El tipo de función es requerido")
    private String functionType = "function5";

    @NotBlank(message = "El tipo de selección es requerido")
    private String selectionType = "roulette";

    @NotBlank(message = "El tipo de cruce es requerido")
    private String crossoverType = "single";

    @NotBlank(message = "El tipo de mutación es requerido")
    private String mutationType = "simple";

    @NotNull(message = "El tamaño de población es requerido")
    @Min(value = 3, message = "El tamaño de población debe ser al menos 3")
    @Max(value = 10000, message = "El tamaño de población no puede exceder 10000")
    private Integer populationSize = 4200;

    @NotNull(message = "El número de generaciones es requerido")
    @Min(value = 3, message = "Las generaciones deben ser al menos 3")
    @Max(value = 5000, message = "Las generaciones no pueden exceder 5000")
    private Integer numGenerations = 30;

    @NotNull(message = "La tasa de mutación es requerida")
    @DecimalMin(value = "0.0", message = "La tasa de mutación debe ser >= 0")
    @DecimalMax(value = "1.0", message = "La tasa de mutación debe ser <= 1")
    private Double mutationRate = 0.001;

    @NotNull(message = "La tasa de cruce es requerida")
    @DecimalMin(value = "0.0", message = "La tasa de cruce debe ser >= 0")
    @DecimalMax(value = "1.0", message = "La tasa de cruce debe ser <= 1")
    private Double crossoverRate = 0.8;

    // Getters y setters
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public List<String> getFileBinaries() { return fileBinaries; }
    public void setFileBinaries(List<String> fileBinaries) { this.fileBinaries = fileBinaries; }

    public Double getXmin() { return xmin; }
    public void setXmin(Double xmin) { this.xmin = xmin; }

    public Double getXmax() { return xmax; }
    public void setXmax(Double xmax) { this.xmax = xmax; }

    public Integer getL() { return L; }
    public void setL(Integer L) { this.L = L; }

    public String getFunctionType() { return functionType; }
    public void setFunctionType(String functionType) { this.functionType = functionType; }

    public String getSelectionType() { return selectionType; }
    public void setSelectionType(String selectionType) { this.selectionType = selectionType; }

    public String getCrossoverType() { return crossoverType; }
    public void setCrossoverType(String crossoverType) { this.crossoverType = crossoverType; }

    public String getMutationType() { return mutationType; }
    public void setMutationType(String mutationType) { this.mutationType = mutationType; }

    public Integer getPopulationSize() { return populationSize; }
    public void setPopulationSize(Integer populationSize) { this.populationSize = populationSize; }

    public Integer getNumGenerations() { return numGenerations; }
    public void setNumGenerations(Integer numGenerations) { this.numGenerations = numGenerations; }

    public Double getMutationRate() { return mutationRate; }
    public void setMutationRate(Double mutationRate) { this.mutationRate = mutationRate; }

    public Double getCrossoverRate() { return crossoverRate; }
    public void setCrossoverRate(Double crossoverRate) { this.crossoverRate = crossoverRate; }
}