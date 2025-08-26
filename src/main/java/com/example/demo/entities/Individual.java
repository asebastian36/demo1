package com.example.demo.entities;

import jakarta.persistence.*;

@Entity
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String binary;
    private Double real;
    private Double adaptative;
    private int generation;

    public Individual() {
        this.binary = "";
        this.real = 0.0;
        this.adaptative = 0.0;
        this.generation = 0;
    }


    public Individual(String binary, Double real, Double adaptative, int generacion) {
        this();
        this.binary = binary;
        this.real = real;
        this.adaptative = adaptative;
        this.generation = generacion;

    }

    public Long getId() {
        return id;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public Double getReal() {
        return real;
    }

    public void setReal(Double real) {
        this.real = real;
    }

    public Double getAdaptative() {
        return adaptative;
    }

    public void setAdaptative(Double adaptative) {
        this.adaptative = adaptative;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generacion) {
        this.generation = generacion;
    }

    @Override
    public String toString() {
        return "Individual{" +
                "id=" + id +
                ", binary='" + binary + '\'' +
                ", real=" + real +
                ", adaptative=" + adaptative +
                ", generation=" + generation +
                '}';
    }
}