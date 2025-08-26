package com.example.demo.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
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

    public String generateAdaptativeChart(List<List<Double>> adaptativeValuesByGeneration) throws IOException {
        // Crear dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Crear series para cada generación
        for (int i = 0; i < adaptativeValuesByGeneration.size(); i++) {
            XYSeries series = new XYSeries("Generación " + (i + 1));
            List<Double> values = adaptativeValuesByGeneration.get(i);

            for (int j = 0; j < values.size(); j++) {
                series.add(j + 1, values.get(j));
            }
            dataset.addSeries(series);
        }

        // Crear el chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución de Valores Adaptativos", // título
                "Individuo (Ordenado)",            // eje X
                "Valor Adaptativo",                // eje Y
                dataset,
                PlotOrientation.VERTICAL,
                true,    // incluir leyenda
                true,    // tooltips
                false    // URLs
        );

        // Personalizar el chart
        XYPlot plot = chart.getXYPlot();

        // Configurar líneas de referencia en Y
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0, 180); // Rango de 0 a 170

        // Agregar líneas de referencia
        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(2, Color.RED, new BasicStroke(2.0f)));
        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(170, Color.RED, new BasicStroke(2.0f)));

        // Personalizar las series
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.getRenderer().setSeriesPaint(2, Color.ORANGE);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(2, new BasicStroke(2.0f));

        // Convertir a base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }
}