package org.example.connectivity;

import org.example.metrics.Metrics;
import org.example.graph.Edge;
import org.example.graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {

    public static List<Integer> sort(Graph graph) {
        return sort(graph, new Metrics());
    }

    public static List<Integer> sort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Topological sort requires a directed graph"
            );
        }

        int n = graph.getVertexCount();
        int[] inDegree = new int[n];

        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;
            inDegree[edge.to()]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();

        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                queue.add(vertex);
                metrics.queuePushes++;
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            metrics.queuePops++;

            order.add(vertex);

            for (Edge edge : graph.neighbors(vertex)) {
                metrics.edgeExaminations++;

                int next = edge.to();
                inDegree[next]--;

                if (inDegree[next] == 0) {
                    queue.add(next);
                    metrics.queuePushes++;
                }
            }
        }

        if (order.size() != n) {
            throw new IllegalArgumentException(
                    "Graph contains a cycle: topological sort is impossible"
            );
        }

        return order;
    }
}