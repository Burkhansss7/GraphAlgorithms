package org.example;

import org.example.connectivity.SCC;
import org.example.connectivity.TopologicalSort;
import org.example.graph.Edge;
import org.example.graph.Graph;
import org.example.graph.GraphGenerator;
import org.example.metrics.Metrics;
import org.example.mst.Kruskal;
import org.example.mst.Prim;
import org.example.shortestpath.Dijkstra;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        testMst();
        testScc();
        testTopologicalSort();
        testDijkstra();
        testGenerator();
        testMetrics();
        testDirectedMetrics();
    }

    private static void testMst() {
        Graph graph = new Graph(5, false);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 5);
        graph.addEdge(2, 3, 8);
        graph.addEdge(3, 4, 3);

        System.out.println("Undirected graph:");
        graph.printGraph();

        printMst("Kruskal", Kruskal.findMst(graph));
        printMst("Prim", Prim.findMst(graph));
    }

    private static void testScc() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        graph.addEdge(2, 3, 1);

        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);

        graph.addEdge(4, 5, 1);

        System.out.println();
        System.out.println("Directed graph:");
        graph.printGraph();

        List<List<Integer>> components = SCC.findComponents(graph);

        System.out.println();
        System.out.println("Strongly connected components:");

        for (int i = 0; i < components.size(); i++) {
            System.out.println(
                    "Component " + (i + 1) + ": " + components.get(i)
            );
        }

        System.out.println("Component count: " + components.size());
    }

    private static void printMst(String algorithm, List<Edge> mst) {
        System.out.println();
        System.out.println(algorithm + " MST:");

        long totalWeight = 0;

        for (Edge edge : mst) {
            System.out.println(edge);
            totalWeight += edge.weight();
        }

        System.out.println("Total weight: " + totalWeight);
    }
    private static void testTopologicalSort() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 4, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1);

        System.out.println();
        System.out.println("Topological sort:");
        System.out.println(TopologicalSort.sort(graph));

        graph.addEdge(5, 1, 1);

        System.out.println();
        System.out.println("Checking a graph with a cycle:");

        try {
            System.out.println(TopologicalSort.sort(graph));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void testDijkstra() {
        Graph graph = new Graph(7, true);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);
        graph.addEdge(3, 4, 3);
        graph.addEdge(4, 5, 1);

        int source = 0;

        Dijkstra.Result result = Dijkstra.shortestPaths(graph, source);

        System.out.println();
        System.out.println("Dijkstra from vertex " + source + ":");

        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            if (result.isReachable(vertex)) {
                System.out.println(
                        "To " + vertex
                                + ": distance = " + result.distanceTo(vertex)
                                + ", path = " + result.pathTo(vertex)
                );
            } else {
                System.out.println("To " + vertex + ": unreachable");
            }
        }
    }
    private static void testGenerator() {
        int[][] sizes = {
                {10, 30},
                {100, 500},
                {1000, 5000}
        };

        long seed = 42L;

        System.out.println();
        System.out.println("Generated graphs:");

        for (int[] size : sizes) {
            int vertices = size[0];
            int edges = size[1];

            Graph undirected = GraphGenerator.connectedUndirected(
                    vertices, edges, seed
            );

            Graph dag = GraphGenerator.dag(
                    vertices, edges, seed
            );

            Graph cyclic = GraphGenerator.directedWithCycle(
                    vertices, edges, seed
            );

            Graph repeated = GraphGenerator.connectedUndirected(
                    vertices, edges, seed
            );

            long kruskalWeight = 0;
            for (Edge edge : Kruskal.findMst(undirected)) {
                kruskalWeight += edge.weight();
            }

            long primWeight = 0;
            for (Edge edge : Prim.findMst(undirected)) {
                primWeight += edge.weight();
            }

            boolean cycleDetected = false;

            try {
                TopologicalSort.sort(cyclic);
            } catch (IllegalArgumentException e) {
                cycleDetected = true;
            }

            System.out.println();
            System.out.println("V = " + vertices + ", E = " + edges);

            System.out.println(
                    "Actual edge counts: "
                            + undirected.getEdges().size() + ", "
                            + dag.getEdges().size() + ", "
                            + cyclic.getEdges().size()
            );

            System.out.println(
                    "Reproducible: "
                            + undirected.getEdges().equals(repeated.getEdges())
            );

            System.out.println(
                    "MST weights match: " + (kruskalWeight == primWeight)
            );

            System.out.println(
                    "DAG ordering size: " + TopologicalSort.sort(dag).size()
            );

            System.out.println(
                    "Cycle detected: " + cycleDetected
            );

            System.out.println(
                    "Cyclic graph SCC count: "
                            + SCC.findComponents(cyclic).size()
            );
        }
    }
    private static void testMetrics() {
        Graph graph = GraphGenerator.connectedUndirected(
                100, 500, 42L
        );

        Metrics kruskalMetrics = new Metrics();
        Metrics primMetrics = new Metrics();

        List<Edge> kruskal = Kruskal.findMst(graph, kruskalMetrics);
        List<Edge> prim = Prim.findMst(graph, primMetrics);

        long kruskalWeight = 0;
        for (Edge edge : kruskal) {
            kruskalWeight += edge.weight();
        }

        long primWeight = 0;
        for (Edge edge : prim) {
            primWeight += edge.weight();
        }

        System.out.println();
        System.out.println("MST operation counts:");

        System.out.println("Kruskal:");
        System.out.println(kruskalMetrics);

        System.out.println("Prim:");
        System.out.println(primMetrics);

        System.out.println(
                "MST weights match: " + (kruskalWeight == primWeight)
        );

        System.out.println(
                "Kruskal union calls: " + kruskalMetrics.unionCalls
        );
    }
    private static void testDirectedMetrics() {
        Graph dag = GraphGenerator.dag(100, 500, 42L);
        Graph cyclic = GraphGenerator.directedWithCycle(100, 500, 42L);

        Metrics sccMetrics = new Metrics();
        Metrics topologicalMetrics = new Metrics();
        Metrics dijkstraMetrics = new Metrics();

        SCC.findComponents(cyclic, sccMetrics);
        TopologicalSort.sort(dag, topologicalMetrics);
        Dijkstra.shortestPaths(cyclic, 0, dijkstraMetrics);

        System.out.println();
        System.out.println("Directed graph operation counts:");

        System.out.println("SCC:");
        System.out.println(sccMetrics);

        System.out.println("Topological sort:");
        System.out.println(topologicalMetrics);

        System.out.println("Dijkstra:");
        System.out.println(dijkstraMetrics);
    }
}