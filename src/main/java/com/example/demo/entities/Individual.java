package com.example.demo.entities;

public class Individual {

    private String binary;
    private Double real;
    private Double adaptative;
    private int generation;

    public Individual(String binary, Double real, Double adaptative, int generation) {
        this.binary = binary;
        this.real = real;
        this.adaptative = adaptative;
        this.generation = generation;
    }

    public Individual() {
    }

    public String getBinary() { return binary; }
    public Double getReal() { return real; }
    public Double getAdaptative() { return adaptative; }
    public int getGeneration() { return generation; }

    @Override
    public String toString() {
        return "Individual{" +
                "binary='" + binary + '\'' +
                ", real=" + real +
                ", adaptative=" + adaptative +
                ", generation=" + generation +
                '}';
    }
}