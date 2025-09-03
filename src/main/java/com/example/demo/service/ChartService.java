package com.example.demo.service;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.*;
import java.util.Base64;
import java.util.List;

@Service
public class ChartService {

    public String generateAdaptativeChart(List<List<Double>> fitnessValuesByGeneration) throws IOException {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < fitnessValuesByGeneration.size(); i++) {
            XYSeries series = new XYSeries("Generación " + (i + 1));
            List<Double> values = fitnessValuesByGeneration.get(i);
            for (int j = 0; j < values.size(); j++) {
                series.add(j + 1, values.get(j));
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Evolución de Valores Adaptativos",
                "Individuo",
                "Valor Adaptativo",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0, 180);

        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(2, Color.RED, new BasicStroke(2.0f)));
        plot.addRangeMarker(new org.jfree.chart.plot.ValueMarker(170, Color.RED, new BasicStroke(2.0f)));

        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.getRenderer().setSeriesPaint(2, Color.ORANGE);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(2, new BasicStroke(2.0f));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        byte[] chartBytes = outputStream.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
    }
}