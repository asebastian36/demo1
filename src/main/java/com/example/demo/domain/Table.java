package com.example.demo.domain;

import java.util.*;

public class Table {
    private List<String> binaries;
    private List<Integer> decimals;
    private List<Double> reals;
    private List<Double> adaptatives;
    private List<Double> orders;

    public Table() {
        binaries = new ArrayList<>();
        decimals = new ArrayList<>();
        reals = new ArrayList<>();
        adaptatives = new ArrayList<>();
    }

    public Table(List<String> binaries,
                 List<Integer> decimals,
                 List<Double> reals,
                 List<Double> adaptatives,
                 List<Double> orders) {
        this.binaries = binaries;
        this.decimals = decimals;
        this.reals = reals;
        this.adaptatives = adaptatives;
        this.orders = orders;
    }

    public List<Integer> getDecimals() {
        return decimals;
    }

    public void setDecimals(List<Integer> decimals) {
        this.decimals = decimals;
    }

    public List<Double> getReals() {
        return reals;
    }

    public void setReals(List<Double> reals) {
        this.reals = reals;
    }

    public List<Double> getAdaptatives() {
        return adaptatives;
    }

    public void setAdaptatives(List<Double> adaptatives) {
        this.adaptatives = adaptatives;
    }

    public List<Double> getOrders() {
        return orders;
    }

    public void setOrders(List<Double> orders) {
        this.orders = orders;
    }

    public List<String> getBinaries() {
        return binaries;
    }

    public void setBinaries(List<String> binaries) {
        this.binaries = binaries;
    }
}
