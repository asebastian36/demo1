package com.example.demo.service.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class ChartService {

    public String generateAdaptativeChart(List<List<Double>> fitnessValuesByGeneration) throws IOException {
        // Creamos una sola serie: "Mejor adaptativo por generación"
        XYSeries bestFitnessSeries = new XYSeries("Mejor Adaptativo por Generación");

        // Recorremos cada generación
        for (int i = 0; i < fitnessValuesByGeneration.size(); i++) {
            List<Double> generationFitness = fitnessValuesByGeneration.get(i);
            if (generationFitness.isEmpty()) continue;

            // Encontramos el MÁXIMO (mejor) valor adaptativo de esta generación
            double maxFitness = generationFitness.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            // Añadimos punto: (número de generación, mejor fitness)
            bestFitnessSeries.add(i + 1, maxFitness);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bestFitnessSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución del Mejor Valor Adaptativo",
                "Generación",
                "Valor Adaptativo (f(x))",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        // Ajustamos el rango Y: máximo esperado es 64 (en x=±3)
        yAxis.setRange(0, 70); // Un poco más que 64 para ver bien

        // Marcador en 64 (valor óptimo que buscamos)
        plot.addRangeMarker(new ValueMarker(64, Color.RED, new BasicStroke(2.0f)));

        // Estilo de la línea
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.5f));

        // Fondo blanco y grid suave
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        // Tamaño de imagen
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }
}