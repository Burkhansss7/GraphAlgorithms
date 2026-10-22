package org.example.connectivity;

import org.example.metrics.Metrics;
import org.example.graph.Edge;
import org.example.graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCC {

    public static List<List<Integer>> findComponents(Graph graph) {
        return findComponents(graph, new Metrics());
    }

    public static List<List<Integer>> findComponents(
            Graph graph, Metrics metrics
    ) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "SCC requires a directed graph"
            );
        }

        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();

        for (int vertex = 0; vertex < n; vertex++) {
            if (!visited[vertex]) {
                fillOrder(graph, vertex, visited, order, metrics);
            }
        }

        Graph reversed = new Graph(n, true);

        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;
            reversed.addEdge(edge.to(), edge.from(), edge.weight());
        }

        visited = new boolean[n];
        Collections.reverse(order);

        List<List<Integer>> components = new ArrayList<>();

        for (int vertex : order) {
            if (!visited[vertex]) {
                List<Integer> component = new ArrayList<>();

                collectComponent(
                        reversed, vertex, visited, component, metrics
                );

                components.add(component);
            }
        }

        return components;
    }

    private static void fillOrder(
            Graph graph,
            int vertex,
            boolean[] visited,
            List<Integer> order,
            Metrics metrics
    ) {
        visited[vertex] = true;

        for (Edge edge : graph.neighbors(vertex)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to()]) {
                fillOrder(graph, edge.to(), visited, order, metrics);
            }
        }

        order.add(vertex);
    }

    private static void collectComponent(
            Graph graph,
            int vertex,
            boolean[] visited,
            List<Integer> component,
            Metrics metrics
    ) {
        visited[vertex] = true;
        component.add(vertex);

        for (Edge edge : graph.neighbors(vertex)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to()]) {
                collectComponent(
                        graph, edge.to(), visited, component, metrics
                );
            }
        }
    }
}