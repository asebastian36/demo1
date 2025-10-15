package com.example.demo.genetic.operators;

public class CrossoverResult {
    private final String[] children;
    private final int[] cutPoints; // Puede ser 1 o 2 puntos

    public CrossoverResult(String[] children, int... cutPoints) {
        this.children = children;
        this.cutPoints = cutPoints;
    }

    public String[] getChildren() {
        return children;
    }

    public int[] getCutPoints() {
        return cutPoints;
    }

    public String getCutPointsString() {
        if (cutPoints == null || cutPoints.length == 0) {
            return "N/A";
        }
        if (cutPoints.length == 1) {
            return String.valueOf(cutPoints[0]);
        }
        return cutPoints[0] + "-" + cutPoints[1];
    }
}
