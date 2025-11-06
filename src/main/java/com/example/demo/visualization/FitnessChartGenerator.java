package com.example.demo.visualization;

import com.example.demo.execution.model.Individual;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FitnessChartGenerator {

    public String generateAdaptativeChart(List<List<Double>> fitnessValuesByGeneration, String functionType) throws IOException {
        // Obtener valor óptimo
        double optimalValue = getOptimalValue(functionType);
        // Ajustamos el eje Y para que se vea el óptimo y el fitness (que puede ser mayor a 10)
        // Usando un valor de 1.5 veces el óptimo para dar espacio al gráfico.
        double yMax = optimalValue * 1.5;
        if (optimalValue == 0.0) yMax = 20.0; // Caso de que optimalValue sea 0

        // Crear serie del Mejor Fitness (la línea azul que faltaba)
        XYSeries bestFitnessSeries = new XYSeries("Mejor Adaptativo");
        // Crear serie del Fitness Promedio (necesaria para contrastar y dar contexto)
        XYSeries avgFitnessSeries = new XYSeries("Adaptativo Promedio");

        for (int i = 0; i < fitnessValuesByGeneration.size(); i++) {
            List<Double> generationFitness = fitnessValuesByGeneration.get(i);
            if (generationFitness.isEmpty()) continue;

            // 1. Obtener el Mejor Fitness (Max)
            double maxFitness = generationFitness.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            // 2. Obtener el Fitness Promedio (Avg)
            double avgFitness = generationFitness.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Aseguramos que el yMax sea suficiente si el fitness inicial es muy alto
            if (maxFitness > yMax) {
                yMax = maxFitness * 1.1;
            }

            bestFitnessSeries.add(i + 1, maxFitness);
            avgFitnessSeries.add(i + 1, avgFitness);
        }

        // Manejo de casos con pocos puntos o uno solo
        if (bestFitnessSeries.getItemCount() == 0) {
            bestFitnessSeries.add(1, 0.0);
            bestFitnessSeries.add(2, 0.0);
            avgFitnessSeries.add(1, 0.0);
            avgFitnessSeries.add(2, 0.0);
        } else if (bestFitnessSeries.getItemCount() == 1) {
            double bestValue = bestFitnessSeries.getY(0).doubleValue();
            double avgValue = avgFitnessSeries.getY(0).doubleValue();
            bestFitnessSeries.add(2, bestValue);
            avgFitnessSeries.add(2, avgValue);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bestFitnessSeries); // Serie 0: Mejor Adaptativo
        dataset.addSeries(avgFitnessSeries);  // Serie 1: Adaptativo Promedio

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución del Valor Adaptativo (" + getFunctionName(functionType) + ")",
                "Generación",
                "Valor Adaptativo (f(x))",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0, yMax); // Rango dinámico basado en el fitness máximo

        // Línea del valor óptimo
        plot.addRangeMarker(new ValueMarker(optimalValue, Color.RED, new BasicStroke(2.0f)));

        // Estilo de las líneas
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        // Configurar el Mejor Fitness (Serie 0) como AZUL
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));

        // Configurar el Fitness Promedio (Serie 1) como NEGRO o GRIS
        renderer.setSeriesPaint(1, Color.BLACK);
        renderer.setSeriesStroke(1, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f)); // Línea punteada/discontinua

        // Opcional: Ocultar puntos para que sólo se vean las líneas
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);


        // Fondo y grid
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }

    public String generateDistributionChart(List<List<Individual>> generations, String functionType) throws IOException {

        final int totalGenerations = generations.size();
        final int minGenerations = 3;

        if (totalGenerations < 2) {
            // Se necesita al menos Inicial y Final.
            return "error:insufficient_data";
        }

        // 1. Seleccionar las poblaciones
        final List<Double> gInitial = generations.get(0).stream().map(Individual::getAdaptative).collect(Collectors.toList());
        final List<Double> gFinal = generations.getLast().stream().map(Individual::getAdaptative).collect(Collectors.toList());

        // Determinar la Generación Intermedia (Gi)
        final List<Double> gIntermediate;
        String intermediateLabel = "N/A";

        if (totalGenerations >= minGenerations) {
            int midIndex = (totalGenerations - 1) / 2;
            gIntermediate = generations.get(midIndex).stream().map(Individual::getAdaptative).collect(Collectors.toList());
            intermediateLabel = "Gen " + (midIndex + 1);
        } else {
            gIntermediate = Collections.emptyList();
        }


        // 2. Crear el Dataset
        final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        // Añadir las poblaciones (JFreeChart necesita listas de Doubles)
        dataset.add(gInitial, "Fitness", "Gen 1 (Inicial)");
        if (!gIntermediate.isEmpty()) {
            dataset.add(gIntermediate, "Fitness", intermediateLabel);
        }
        dataset.add(gFinal, "Fitness", "Gen " + totalGenerations + " (Final)");


        // 3. Crear el Gráfico (Box Plot)
        final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                "Distribución del Fitness: Inicial, Media y Final",
                "Generación/Población",
                "Valor Adaptativo",
                dataset,
                true
        );

        // 4. Estilizar y Renderizar
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();

        // Estilos para el Box Plot
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(true);
        renderer.setMeanVisible(true); // Mostrar la media (punto)

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);


        // 5. Convertir a Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 400);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }

    private double getOptimalValue(String functionType) {
        switch (functionType) {
            case "consumo":
                return 14.75;
            case "function5":
                return 64.0;
            case "function2":
                return 173.0;
            case "farmacologia":
                return 100.0;
            default:
                return 1.0;
        }
    }

    private String getFunctionName(String functionType) {
        switch (functionType) {
            case "credit":
                return "Riesgo Crediticio";
            case "consumo":
                return "Preferencias de Consumo";
            case "function5", "function2":
                return "Función Cuadrática";
            case "farmacologia":
                return "Optimización Farmacológica";
            default:
                return functionType;
        }
    }
}