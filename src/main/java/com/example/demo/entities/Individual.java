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

    public Individual(String binary, Double real, Double adaptative, int generation) {
        this.binary = binary;
        this.real = real;
        this.adaptative = adaptative;
        this.generation = generation;
    }

    public Individual() {

    }

    public Long getId() { return id; }
    public String getBinary() { return binary; }
    public Double getReal() { return real; }
    public Double getAdaptative() { return adaptative; }
    public int getGeneration() { return generation; }

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