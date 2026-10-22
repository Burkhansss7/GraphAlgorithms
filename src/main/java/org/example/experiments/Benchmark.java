package org.example.experiments;

import org.example.connectivity.SCC;
import org.example.connectivity.TopologicalSort;
import org.example.graph.Edge;
import org.example.graph.Graph;
import org.example.graph.GraphGenerator;
import org.example.metrics.Metrics;
import org.example.mst.Kruskal;
import org.example.mst.Prim;
import org.example.shortestpath.Dijkstra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class Benchmark {

    private static final int WARMUP_RUNS = 20;
    private static final int MEASURED_RUNS = 5;
    private static final long SEED = 42L;

    private static volatile Object resultSink;

    @FunctionalInterface
    private interface Algorithm {
        Object run(Metrics metrics);
    }

    public static void main(String[] args) throws IOException {
        int[][] sizes = {
                {10, 30},
                {100, 500},
                {1000, 5000}
        };

        StringBuilder raw = new StringBuilder(
                "algorithm,graph_type,vertices,edges,seed,run,time_ns,"
                        + "edge_examinations,comparisons,find_calls,"
                        + "union_calls,queue_pushes,queue_pops\n"
        );

        StringBuilder summary = new StringBuilder(
                "algorithm,graph_type,vertices,edges,seed,runs,"
                        + "avg_time_ns,avg_time_ms,"
                        + "avg_edge_examinations,avg_comparisons,"
                        + "avg_find_calls,avg_union_calls,"
                        + "avg_queue_pushes,avg_queue_pops\n"
        );

        for (int[] size : sizes) {
            int vertices = size[0];
            int edges = size[1];

            Graph undirected = GraphGenerator.connectedUndirected(
                    vertices, edges, SEED
            );

            Graph dag = GraphGenerator.dag(
                    vertices, edges, SEED
            );

            Graph cyclic = GraphGenerator.directedWithCycle(
                    vertices, edges, SEED
            );

            verifyGraphs(undirected, dag, cyclic);

            measure(
                    "Kruskal", "undirected",
                    undirected,
                    metrics -> Kruskal.findMst(undirected, metrics),
                    raw, summary
            );

            measure(
                    "Prim", "undirected",
                    undirected,
                    metrics -> Prim.findMst(undirected, metrics),
                    raw, summary
            );

            measure(
                    "SCC", "dag",
                    dag,
                    metrics -> SCC.findComponents(dag, metrics),
                    raw, summary
            );

            measure(
                    "SCC", "cyclic",
                    cyclic,
                    metrics -> SCC.findComponents(cyclic, metrics),
                    raw, summary
            );

            measure(
                    "TopologicalSort", "dag",
                    dag,
                    metrics -> TopologicalSort.sort(dag, metrics),
                    raw, summary
            );

            measure(
                    "TopologicalSort", "cyclic",
                    cyclic,
                    metrics -> detectCycle(cyclic, metrics),
                    raw, summary
            );

            measure(
                    "Dijkstra", "cyclic",
                    cyclic,
                    metrics -> Dijkstra.shortestPaths(cyclic, 0, metrics),
                    raw, summary
            );
        }

        Path directory = Path.of("results", "tables");
        Files.createDirectories(directory);

        Path rawFile = directory.resolve("raw_results.csv");
        Path summaryFile = directory.resolve("summary.csv");
        Path environmentFile = directory.resolve("environment.txt");

        Files.writeString(
                rawFile, raw.toString(), StandardCharsets.UTF_8
        );

        Files.writeString(
                summaryFile, summary.toString(), StandardCharsets.UTF_8
        );

        String environment =
                "Java version: " + System.getProperty("java.version")
                        + "\nJVM: " + System.getProperty("java.vm.name")
                        + "\nOS: " + System.getProperty("os.name")
                        + "\nArchitecture: " + System.getProperty("os.arch")
                        + "\nAvailable processors: "
                        + Runtime.getRuntime().availableProcessors()
                        + "\nSeed: " + SEED
                        + "\nWarmup runs: " + WARMUP_RUNS
                        + "\nMeasured runs: " + MEASURED_RUNS
                        + "\n";

        Files.writeString(
                environmentFile, environment, StandardCharsets.UTF_8
        );

        System.out.println();
        System.out.println("Saved:");
        System.out.println(rawFile.toAbsolutePath());
        System.out.println(summaryFile.toAbsolutePath());
        System.out.println(environmentFile.toAbsolutePath());
    }

    private static void measure(
            String name,
            String graphType,
            Graph graph,
            Algorithm algorithm,
            StringBuilder raw,
            StringBuilder summary
    ) {
        for (int run = 0; run < WARMUP_RUNS; run++) {
            resultSink = algorithm.run(new Metrics());
        }

        long totalTime = 0;
        Metrics totals = new Metrics();

        for (int run = 1; run <= MEASURED_RUNS; run++) {
            Metrics metrics = new Metrics();

            long start = System.nanoTime();
            Object result = algorithm.run(metrics);
            long elapsed = System.nanoTime() - start;

            resultSink = result;

            totalTime += elapsed;
            accumulate(totals, metrics);

            raw.append(String.format(
                    Locale.US,
                    "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                    name,
                    graphType,
                    graph.getVertexCount(),
                    graph.getEdges().size(),
                    SEED,
                    run,
                    elapsed,
                    metrics.edgeExaminations,
                    metrics.comparisons,
                    metrics.findCalls,
                    metrics.unionCalls,
                    metrics.queuePushes,
                    metrics.queuePops
            ));
        }

        double averageNs = totalTime / (double) MEASURED_RUNS;
        double averageMs = averageNs / 1_000_000.0;

        summary.append(String.format(
                Locale.US,
                "%s,%s,%d,%d,%d,%d,%.2f,%.6f,"
                        + "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                name,
                graphType,
                graph.getVertexCount(),
                graph.getEdges().size(),
                SEED,
                MEASURED_RUNS,
                averageNs,
                averageMs,
                totals.edgeExaminations / (double) MEASURED_RUNS,
                totals.comparisons / (double) MEASURED_RUNS,
                totals.findCalls / (double) MEASURED_RUNS,
                totals.unionCalls / (double) MEASURED_RUNS,
                totals.queuePushes / (double) MEASURED_RUNS,
                totals.queuePops / (double) MEASURED_RUNS
        ));

        System.out.printf(
                Locale.US,
                "%-16s %-10s V=%4d E=%5d average=%.6f ms%n",
                name,
                graphType,
                graph.getVertexCount(),
                graph.getEdges().size(),
                averageMs
        );
    }

    private static void accumulate(Metrics totals, Metrics current) {
        totals.edgeExaminations += current.edgeExaminations;
        totals.comparisons += current.comparisons;
        totals.findCalls += current.findCalls;
        totals.unionCalls += current.unionCalls;
        totals.queuePushes += current.queuePushes;
        totals.queuePops += current.queuePops;
    }

    private static boolean detectCycle(Graph graph, Metrics metrics) {
        try {
            TopologicalSort.sort(graph, metrics);
        } catch (IllegalArgumentException e) {
            return true;
        }

        throw new IllegalStateException(
                "Expected a cycle, but topological sorting succeeded"
        );
    }

    private static void verifyGraphs(
            Graph undirected, Graph dag, Graph cyclic
    ) {
        long kruskalWeight = totalWeight(Kruskal.findMst(undirected));
        long primWeight = totalWeight(Prim.findMst(undirected));

        if (kruskalWeight != primWeight) {
            throw new IllegalStateException("MST weights do not match");
        }

        if (TopologicalSort.sort(dag).size() != dag.getVertexCount()) {
            throw new IllegalStateException("Invalid DAG ordering size");
        }

        detectCycle(cyclic, new Metrics());

        if (SCC.findComponents(cyclic).size() != 1) {
            throw new IllegalStateException(
                    "The generated directed ring must be strongly connected"
            );
        }
    }

    private static long totalWeight(List<Edge> edges) {
        long total = 0;

        for (Edge edge : edges) {
            total += edge.weight();
        }

        return total;
    }
}