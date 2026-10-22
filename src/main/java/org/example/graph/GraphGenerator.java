package org.example.graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {

    public static Graph connectedUndirected(
            int vertices, int edges, long seed
    ) {
        long maxEdges = (long) vertices * (vertices - 1) / 2;
        validate(vertices, edges, maxEdges);

        if (edges < vertices - 1) {
            throw new IllegalArgumentException(
                    "A connected graph needs at least V - 1 edges"
            );
        }

        Graph graph = new Graph(vertices, false);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();

        for (int vertex = 1; vertex < vertices; vertex++) {
            int parent = random.nextInt(vertex);

            addUniqueEdge(
                    graph, used, parent, vertex, random, true
            );
        }

        while (graph.getEdges().size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);

            if (from != to) {
                addUniqueEdge(
                        graph, used, from, to, random, true
                );
            }
        }

        return graph;
    }

    public static Graph dag(int vertices, int edges, long seed) {
        long maxEdges = (long) vertices * (vertices - 1) / 2;
        validate(vertices, edges, maxEdges);

        Graph graph = new Graph(vertices, true);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();

        while (graph.getEdges().size() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);

            if (a != b) {
                int from = Math.min(a, b);
                int to = Math.max(a, b);

                addUniqueEdge(
                        graph, used, from, to, random, false
                );
            }
        }

        return graph;
    }

    public static Graph directedWithCycle(
            int vertices, int edges, long seed
    ) {
        long maxEdges = (long) vertices * (vertices - 1);
        validate(vertices, edges, maxEdges);

        if (vertices < 2 || edges < vertices) {
            throw new IllegalArgumentException(
                    "This generator needs V >= 2 and E >= V"
            );
        }

        Graph graph = new Graph(vertices, true);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();

        for (int vertex = 0; vertex < vertices; vertex++) {
            int next = (vertex + 1) % vertices;

            addUniqueEdge(
                    graph, used, vertex, next, random, false
            );
        }

        while (graph.getEdges().size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);

            if (from != to) {
                addUniqueEdge(
                        graph, used, from, to, random, false
                );
            }
        }

        return graph;
    }

    private static void addUniqueEdge(
            Graph graph,
            Set<Long> used,
            int from,
            int to,
            Random random,
            boolean undirected
    ) {
        if (undirected && from > to) {
            int temp = from;
            from = to;
            to = temp;
        }

        long key = ((long) from << 32) | (to & 0xffffffffL);

        if (used.add(key)) {
            int weight = random.nextInt(100) + 1;
            graph.addEdge(from, to, weight);
        }
    }

    private static void validate(
            int vertices, int edges, long maxEdges
    ) {
        if (vertices < 1) {
            throw new IllegalArgumentException(
                    "Vertex count must be positive"
            );
        }

        if (edges < 0 || edges > maxEdges) {
            throw new IllegalArgumentException(
                    "Invalid edge count: " + edges
            );
        }
    }
}