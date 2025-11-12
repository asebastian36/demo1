package com.example.demo.function.transporte;

import com.example.demo.function.AbstractSegmentedFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("transporte")
public class TransportRouteFitnessFunction extends AbstractSegmentedFitnessFunction {

    private static final Logger log = LoggerFactory.getLogger(TransportRouteFitnessFunction.class);

    // Matriz de datos: (Distancia, Costo, Pasajeros)
    private static final double[][] PARADA_DATA = {
            {10.0, 80.0, 150.0}, // Bit 0: Nezahualcóyotl
            {20.0, 100.0, 120.0},// Bit 1: Chimalhuacán
            {35.0, 150.0, 200.0},// Bit 2: Texcoco
            {25.0, 120.0, 180.0},// Bit 3: Ecatepec
            {30.0, 130.0, 160.0} // Bit 4: Tlalnepantla
    };

    private static final String[] VARIABLE_NAMES = {
            "Nezahualcóyotl", "Chimalhuacán", "Texcoco", "Ecatepec", "Tlalnepantla"
    };

    private static final double ALPHA = 0.5; // Peso de penalización del costo
    private static final int TOTAL_LENGTH = 5;

    @Override
    public double evaluate(String binary) {
        if (binary == null || binary.length() != TOTAL_LENGTH) {
            log.error("Cromosoma inválido para transporte: longitud={}", binary != null ? binary.length() : 0);
            return 0.0;
        }

        double totalP = 0.0; // Pasajeros
        double totalD = 0.0; // Distancia
        double totalC = 0.0; // Costo

        // 1. Decodificación y suma de métricas (P, D, C)
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            // El gen es 1 si la parada está incluida
            if (binary.charAt(i) == '1') {
                totalD += PARADA_DATA[i][0]; // Distancia
                totalC += PARADA_DATA[i][1]; // Costo
                totalP += PARADA_DATA[i][2]; // Pasajeros
            }
        }

        // 2. Cálculo del Fitness (F)
        // F = P / (D + αC)
        double denominator = totalD + (ALPHA * totalC);

        if (denominator <= 0) {
            // Caso de que la ruta esté vacía (00000) o haya un error.
            // Una ruta vacía tiene P=0, D=0, C=0, denominador=0. F=0.
            return 0.0;
        }

        double fitness = totalP / denominator;

        // El valor de fitness debe ser siempre positivo (maximización)
        return Math.max(fitness, 0.0);
    }

    // --- Métodos de Interpretación y Configuración (Para ChromosomeBasedFitnessFunction) ---

    @Override
    public Map<String, Object> decodeAndInterpret(String binary, com.example.demo.io.conversion.BinaryToDecimalConverter converter) {

        Map<String, Object> interpretation = new LinkedHashMap<>();

        double totalP = 0.0;
        double totalD = 0.0;
        double totalC = 0.0;
        StringBuilder activeStops = new StringBuilder("CDMX Centro (Origen) -> ");

        // Decodificación
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            if (binary.charAt(i) == '1') {
                activeStops.append(VARIABLE_NAMES[i]).append(" -> ");
                totalD += PARADA_DATA[i][0];
                totalC += PARADA_DATA[i][1];
                totalP += PARADA_DATA[i][2];
            }
        }
        activeStops.append("CDMX Centro (Destino)");

        double fitness = evaluate(binary);

        interpretation.put("Ruta Activa", activeStops.toString());
        interpretation.put("Distancia (D)", String.format("%.1f km", totalD));
        interpretation.put("Costo (C)", String.format("$%.0f", totalC));
        interpretation.put("Pasajeros (P)", String.format("%.0f", totalP));
        interpretation.put("Fitness (F)", String.format("%.4f", fitness));
        interpretation.put("Interpretación", getInterpretation(fitness));

        return interpretation;
    }

    @Override
    public int[] getSegmentLengths() {
        // No aplicable para este tipo de cromosoma (es un solo segmento lógico)
        return new int[]{TOTAL_LENGTH};
    }

    @Override
    public String[] getVariableNames() {
        return VARIABLE_NAMES;
    }

    @Override
    protected double[][] getRanges() {
        // Rangos son solo para el decodificador, aquí son implícitos (0 o 1)
        return Collections.emptyList().toArray(new double[0][0]);
    }

    @Override
    protected String getInterpretation(double fitness) {
        if (fitness >= 1.30) return "Ruta EXCELENTE (Alta eficiencia)";
        else if (fitness >= 1.20) return "Ruta BUENA (Eficiencia balanceada)";
        else if (fitness >= 1.0) return "Ruta ACEPTABLE (Poca eficiencia)";
        else return "Ruta INEFICIENTE (Costos altos)";
    }

    @Override
    protected int getTotalLength() {
        return TOTAL_LENGTH;
    }

    @Override
    public String getName() {
        return "Optimización de Rutas de Transporte";
    }

    @Override
    public double getOptimalValue() {
        // Basado en el ejemplo, el valor más alto encontrado es 1.32.
        // Asumimos un máximo ligeramente superior como objetivo teórico.
        return 3;
    }

    @Override
    public double getTargetX() {
        // En funciones de cromosoma, TargetX debe ser el óptimo de fitness.
        return getOptimalValue();
    }
}