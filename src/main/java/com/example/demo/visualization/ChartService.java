package com.example.demo.visualization;

import com.example.demo.genetic.function.FitnessFunction;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ChartService {

    private final Map<String, FitnessFunction> fitnessFunctions;

    public ChartService(Map<String, FitnessFunction> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public String generateAdaptativeChart(List<List<Double>> fitnessValuesByGeneration, String functionType) throws IOException {
        FitnessFunction function = fitnessFunctions.get(functionType);
        if (function == null) {
            throw new IllegalArgumentException("Función desconocida para gráfica: " + functionType);
        }

        double optimalValue = function.getOptimalValue();
        // Ajustamos el rango Y: 10% más que el óptimo para ver bien la línea
        double yMax = optimalValue * 1.1;

        // Creamos la serie del mejor fitness por generación
        XYSeries bestFitnessSeries = new XYSeries("Mejor Adaptativo por Generación");

        for (int i = 0; i < fitnessValuesByGeneration.size(); i++) {
            List<Double> generationFitness = fitnessValuesByGeneration.get(i);
            if (generationFitness.isEmpty()) continue;

            double maxFitness = generationFitness.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            bestFitnessSeries.add(i + 1, maxFitness);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bestFitnessSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución del Mejor Valor Adaptativo (" + function.getName() + ")",
                "Generación",
                "Valor Adaptativo (f(x))",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0, yMax);

        // Línea del valor óptimo (dinámico)
        plot.addRangeMarker(new ValueMarker(optimalValue, Color.RED, new BasicStroke(2.0f)));

        // Estilo de la línea
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.5f));

        // Fondo y grid
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }
}