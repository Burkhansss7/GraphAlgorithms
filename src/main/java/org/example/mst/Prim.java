package org.example.mst;

import org.example.metrics.Metrics;
import org.example.graph.Edge;
import org.example.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static List<Edge> findMst(Graph graph) {
        return findMst(graph, new Metrics());
    }

    public static List<Edge> findMst(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph"
            );
        }

        int n = graph.getVertexCount();
        List<Edge> mst = new ArrayList<>();

        if (n == 0) {
            return mst;
        }

        boolean[] visited = new boolean[n];

        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        visited[0] = true;

        for (Edge edge : graph.neighbors(0)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to()]) {
                queue.add(edge);
                metrics.queuePushes++;
            }
        }

        while (!queue.isEmpty() && mst.size() < n - 1) {
            Edge edge = queue.poll();
            metrics.queuePops++;

            int next = edge.to();

            if (visited[next]) {
                continue;
            }

            visited[next] = true;
            mst.add(edge);

            for (Edge neighbor : graph.neighbors(next)) {
                metrics.edgeExaminations++;

                if (!visited[neighbor.to()]) {
                    queue.add(neighbor);
                    metrics.queuePushes++;
                }
            }
        }

        if (mst.size() != n - 1) {
            throw new IllegalArgumentException(
                    "Graph is disconnected: MST does not exist"
            );
        }

        return mst;
    }
}