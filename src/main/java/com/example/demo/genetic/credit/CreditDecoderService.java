package com.example.demo.genetic.creit;

import org.springframework.stereotype.Service;

@Service
public class CreditDecoderService {

    public double decodeIncome(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return 5000 + (decimal / 255.0) * 95000;
    }

    public double decodeAge(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return 18 + (decimal / 63.0) * 52;
    }

    public double decodeHistory(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return decimal / 63.0;
    }

    public double decodeDebt(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return (decimal / 127.0) * 100;
    }

    public double decodeSavings(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return (decimal / 127.0) * 100;
    }

    public CreditProfile decodeProfile(String chromosome) {
        double income = decodeIncome(chromosome.substring(0, 8));
        double age = decodeAge(chromosome.substring(8, 14));
        double history = decodeHistory(chromosome.substring(14, 20));
        double debt = decodeDebt(chromosome.substring(20, 27));
        double savings = decodeSavings(chromosome.substring(27, 34));
        double fitness = 0.3 * ((income - 5000) / 95000.0) +
                0.1 * ((age - 18) / 52.0) +
                0.25 * history +
                0.2 * (1 - (debt / 100.0)) +
                0.15 * (savings / 100.0);

        return new CreditProfile(income, age, history, debt, savings, fitness);
    }

    public static class CreditProfile {
        private final double income;
        private final double age;
        private final double history;
        private final double debt;
        private final double savings;
        private final double fitness;

        public CreditProfile(double income, double age, double history, double debt, double savings, double fitness) {
            this.income = income;
            this.age = age;
            this.history = history;
            this.debt = debt;
            this.savings = savings;
            this.fitness = fitness;
        }

        // Getters
        public double getIncome() { return income; }
        public double getAge() { return age; }
        public double getHistory() { return history; }
        public double getDebt() { return debt; }
        public double getSavings() { return savings; }
        public double getFitness() { return fitness; }

        public String getRiskLevel() {
            if (fitness >= 0.81) return "Muy alto";
            if (fitness >= 0.61) return "Alto";
            if (fitness >= 0.31) return "Medio";
            return "Bajo";
        }

        public String getRecommendation() {
            if (fitness >= 0.81) return "Cliente ideal - Aprobar crédito";
            if (fitness >= 0.61) return "Cliente confiable - Aprobar crédito";
            if (fitness >= 0.31) return "Cliente aceptable - Requiere garantías";
            return "Alto riesgo - Rechazar crédito";
        }
    }
}