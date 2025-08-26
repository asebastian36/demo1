package com.example.demo.service;

import com.example.demo.utils.IntPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CrossConverterService {

    @Autowired
    private BinaryConverterService binaryConverterService;

    @Autowired
    private RealConverterService realConverterService;

    @Autowired
    private AdaptitiveConverterService adaptitiveConverterService;

    public List<String> cruceGeneracion(List<String> binaryNumbers, List<IntPair> cruce,
                                        double xmin, double xmax, int L) {
        List<String> nuevaGeneracion = new ArrayList<>(binaryNumbers);

        System.out.println("Iniciando proceso de cruce...");
        System.out.println("Binarios antes de cruces: " + nuevaGeneracion);

        for (IntPair par : cruce) {
            int index1 = par.first() - 1;
            int index2 = par.second() - 1;

            if (index1 >= 0 && index1 < nuevaGeneracion.size() &&
                    index2 >= 0 && index2 < nuevaGeneracion.size()) {

                String binario1Original = nuevaGeneracion.get(index1);
                String binario2Original = nuevaGeneracion.get(index2);

                // Asegurar que ambos binarios tengan longitud L
                binario1Original = binaryConverterService.normalizeBinary(binario1Original, L);
                binario2Original = binaryConverterService.normalizeBinary(binario2Original, L);

                // Realizar el cruce en el punto 4 (después del 4to bit)
                String[] hijos = realizarCruce(binario1Original, binario2Original);
                String hijo1 = hijos[0];
                String hijo2 = hijos[1];

                // Calcular adaptativos
                double adaptativo1Original = calcularAdaptativo(binario1Original, xmin, xmax, L);
                double adaptativo2Original = calcularAdaptativo(binario2Original, xmin, xmax, L);
                double adaptativoHijo1 = calcularAdaptativo(hijo1, xmin, xmax, L);
                double adaptativoHijo2 = calcularAdaptativo(hijo2, xmin, xmax, L);

                System.out.println("\n--- CRUCE ---");
                System.out.println("Índices: " + (index1+1) + " ↔ " + (index2+1));
                System.out.println("Original 1 [" + (index1+1) + "]: " + binario1Original + " → Adapt: " + adaptativo1Original);
                System.out.println("Original 2 [" + (index2+1) + "]: " + binario2Original + " → Adapt: " + adaptativo2Original);
                System.out.println("Hijo 1: " + hijo1 + " → Adapt: " + adaptativoHijo1);
                System.out.println("Hijo 2: " + hijo2 + " → Adapt: " + adaptativoHijo2);
                System.out.println("Decisión: " +
                        (adaptativoHijo1 > adaptativo1Original ? "REEMPLAZAR índice " + (index1+1) : "MANTENER índice " + (index1+1)) + ", " +
                        (adaptativoHijo2 > adaptativo2Original ? "REEMPLAZAR índice " + (index2+1) : "MANTENER índice " + (index2+1)));

                // Reemplazar solo si los hijos son mejores
                if (adaptativoHijo1 > adaptativo1Original) {
                    nuevaGeneracion.set(index1, hijo1);
                }
                if (adaptativoHijo2 > adaptativo2Original) {
                    nuevaGeneracion.set(index2, hijo2);
                }
            }
        }

        System.out.println("Binarios después de todos los cruces: " + nuevaGeneracion);
        return nuevaGeneracion;
    }

    private String[] realizarCruce(String binario1, String binario2) {
        if (binario1.length() != binario2.length()) {
            throw new IllegalArgumentException("Binarios de diferente longitud: " + binario1 + " vs " + binario2);
        }

        String hijo1 = binario2.substring(0, 4) + binario1.substring(4);
        String hijo2 = binario1.substring(0, 4) + binario2.substring(4);

        return new String[]{hijo1, hijo2};
    }

    // Y entonces el método sería:
    private double calcularAdaptativo(String binario, double xmin, double xmax, int L) {
        try {
            int decimal = binaryConverterService.convertBinaryToInt(binario);
            double real = realConverterService.toRealSingle(decimal, xmin, xmax, L);
            return adaptitiveConverterService.toAdaptiveSingle(real);
        } catch (Exception e) {
            System.out.println("Error calculando adaptativo para: " + binario);
            return Double.NEGATIVE_INFINITY;
        }
    }
}