package org.example.experiments;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class PlotResults {

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 850;

    private static final int LEFT = 120;
    private static final int RIGHT = 350;
    private static final int TOP = 100;
    private static final int BOTTOM = 110;

    private static final Color[] COLORS = {
            new Color(37, 99, 235),
            new Color(220, 38, 38),
            new Color(22, 163, 74),
            new Color(147, 51, 234),
            new Color(234, 88, 12),
            new Color(8, 145, 178),
            new Color(190, 24, 93)
    };

    private record Row(
            String algorithm,
            String graphType,
            int vertices,
            int edges,
            double timeMs
    ) {
    }

    public static void main(String[] args) throws IOException {
        Path input = Path.of("results", "tables", "summary.csv");

        List<Row> rows = readResults(input);

        Path directory = Path.of("results", "plots");
        Files.createDirectories(directory);

        Path verticesPlot = directory.resolve("time_vs_vertices.png");
        Path edgesPlot = directory.resolve("time_vs_edges.png");

        drawPlot(rows, true, verticesPlot);
        drawPlot(rows, false, edgesPlot);

        System.out.println("Saved:");
        System.out.println(verticesPlot.toAbsolutePath());
        System.out.println(edgesPlot.toAbsolutePath());
    }

    private static List<Row> readResults(Path input) throws IOException {
        List<String> lines = Files.readAllLines(
                input, StandardCharsets.UTF_8
        );

        List<Row> rows = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }

            String[] values = lines.get(i).split(",");

            Row row = new Row(
                    values[0],
                    values[1],
                    Integer.parseInt(values[2]),
                    Integer.parseInt(values[3]),
                    Double.parseDouble(values[7])
            );

            if (!Double.isFinite(row.timeMs()) || row.timeMs() <= 0) {
                throw new IllegalArgumentException(
                        "Execution time must be positive and finite"
                );
            }

            rows.add(row);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("No benchmark results found");
        }

        return rows;
    }

    private static void drawPlot(
            List<Row> rows, boolean useVertices, Path output
    ) throws IOException {
        BufferedImage image = new BufferedImage(
                WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = image.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = HEIGHT - TOP - BOTTOM;

        TreeSet<Integer> xValues = new TreeSet<>();
        Map<String, List<Row>> series = new LinkedHashMap<>();

        double minTime = Double.POSITIVE_INFINITY;
        double maxTime = 0;

        for (Row row : rows) {
            xValues.add(xValue(row, useVertices));

            minTime = Math.min(minTime, row.timeMs());
            maxTime = Math.max(maxTime, row.timeMs());

            String label = row.algorithm() + " (" + row.graphType() + ")";

            series.computeIfAbsent(
                    label, key -> new ArrayList<>()
            ).add(row);
        }

        double minX = Math.log10(xValues.first()) - 0.1;
        double maxX = Math.log10(xValues.last()) + 0.1;

        double minY = Math.floor(Math.log10(minTime));
        double maxY = Math.ceil(Math.log10(maxTime));

        if (minY == maxY) {
            maxY++;
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.setColor(Color.BLACK);

        String title = useVertices
                ? "Execution time vs number of vertices"
                : "Execution time vs number of edges";

        g.drawString(title, LEFT, 45);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.drawString(
                "Average of 5 runs; both axes use logarithmic scales",
                LEFT, 75
        );

        for (int value : xValues) {
            int x = mapX(value, minX, maxX, plotWidth);

            g.setColor(new Color(225, 225, 225));
            g.drawLine(x, TOP, x, TOP + plotHeight);

            g.setColor(Color.DARK_GRAY);
            String label = Integer.toString(value);

            g.drawString(
                    label,
                    x - g.getFontMetrics().stringWidth(label) / 2,
                    TOP + plotHeight + 30
            );
        }

        for (int exponent = (int) minY; exponent <= (int) maxY; exponent++) {
            double value = Math.pow(10, exponent);
            int y = mapY(value, minY, maxY, plotHeight);

            g.setColor(new Color(225, 225, 225));
            g.drawLine(LEFT, y, LEFT + plotWidth, y);

            g.setColor(Color.DARK_GRAY);

            String label = String.format(Locale.US, "%.3g", value);

            g.drawString(
                    label,
                    LEFT - g.getFontMetrics().stringWidth(label) - 15,
                    y + 5
            );
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(LEFT, TOP, plotWidth, plotHeight);

        String xLabel = useVertices ? "Vertices" : "Edges";
        g.drawString(
                xLabel,
                LEFT + plotWidth / 2 - 30,
                HEIGHT - 35
        );

        g.rotate(-Math.PI / 2);
        String yLabel = "Average execution time (ms)";

        g.drawString(
                yLabel,
                -(TOP + plotHeight / 2)
                        - g.getFontMetrics().stringWidth(yLabel) / 2,
                30
        );

        g.rotate(Math.PI / 2);

        int seriesIndex = 0;

        for (Map.Entry<String, List<Row>> entry : series.entrySet()) {
            Color color = COLORS[seriesIndex % COLORS.length];

            List<Row> points = entry.getValue();
            points.sort(
                    Comparator.comparingInt(row -> xValue(row, useVertices))
            );

            g.setColor(color);
            g.setStroke(new BasicStroke(2.5f));

            Integer previousX = null;
            Integer previousY = null;

            for (Row row : points) {
                int x = mapX(
                        xValue(row, useVertices), minX, maxX, plotWidth
                );

                int y = mapY(
                        row.timeMs(), minY, maxY, plotHeight
                );

                if (previousX != null) {
                    g.drawLine(previousX, previousY, x, y);
                }

                g.fillOval(x - 5, y - 5, 10, 10);

                previousX = x;
                previousY = y;
            }

            int legendX = LEFT + plotWidth + 30;
            int legendY = TOP + 25 + seriesIndex * 38;

            g.drawLine(legendX, legendY, legendX + 25, legendY);
            g.fillOval(legendX + 8, legendY - 4, 8, 8);

            g.setColor(Color.DARK_GRAY);
            g.drawString(entry.getKey(), legendX + 35, legendY + 5);

            seriesIndex++;
        }

        g.dispose();
        ImageIO.write(image, "png", output.toFile());
    }

    private static int xValue(Row row, boolean useVertices) {
        return useVertices ? row.vertices() : row.edges();
    }

    private static int mapX(
            double value, double min, double max, int plotWidth
    ) {
        return LEFT + (int) Math.round(
                (Math.log10(value) - min) / (max - min) * plotWidth
        );
    }

    private static int mapY(
            double value, double min, double max, int plotHeight
    ) {
        return TOP + plotHeight - (int) Math.round(
                (Math.log10(value) - min) / (max - min) * plotHeight
        );
    }
}