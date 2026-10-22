package org.example.mst;

import org.example.metrics.Metrics;
import org.example.dsu.DSU;
import org.example.graph.Edge;
import org.example.graph.Graph;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

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
        DSU dsu = new DSU(n, metrics);

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());

        sortedEdges.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        List<Edge> mst = new ArrayList<>();

        for (Edge edge : sortedEdges) {
            metrics.edgeExaminations++;

            if (dsu.union(edge.from(), edge.to())) {
                mst.add(edge);

                if (mst.size() == n - 1) {
                    break;
                }
            }
        }

        if (n > 0 && mst.size() != n - 1) {
            throw new IllegalArgumentException(
                    "Graph is disconnected: MST does not exist"
            );
        }

        return mst;
    }
}