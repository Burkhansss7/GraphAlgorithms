package org.example.shortestpath;

import org.example.metrics.Metrics;
import org.example.graph.Edge;
import org.example.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    public static Result shortestPaths(Graph graph, int source) {
        return shortestPaths(graph, source, new Metrics());
    }

    public static Result shortestPaths(
            Graph graph, int source, Metrics metrics
    ) {
        int n = graph.getVertexCount();

        if (source < 0 || source >= n) {
            throw new IllegalArgumentException(
                    "Invalid source vertex: " + source
            );
        }

        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;

            if (edge.weight() < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra requires non-negative edge weights"
                );
            }
        }

        long[] distances = new long[n];
        int[] previous = new int[n];

        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(previous, -1);

        distances[source] = 0;

        PriorityQueue<State> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Long.compare(a.distance(), b.distance());
        });

        queue.add(new State(source, 0));
        metrics.queuePushes++;

        while (!queue.isEmpty()) {
            State current = queue.poll();
            metrics.queuePops++;

            int vertex = current.vertex();

            if (current.distance() != distances[vertex]) {
                continue;
            }

            for (Edge edge : graph.neighbors(vertex)) {
                metrics.edgeExaminations++;

                int next = edge.to();
                long newDistance = distances[vertex] + edge.weight();

                if (newDistance < distances[next]) {
                    distances[next] = newDistance;
                    previous[next] = vertex;

                    queue.add(new State(next, newDistance));
                    metrics.queuePushes++;
                }
            }
        }

        return new Result(source, distances, previous);
    }

    private record State(int vertex, long distance) {
    }

    public static class Result {

        private final int source;
        private final long[] distances;
        private final int[] previous;

        private Result(int source, long[] distances, int[] previous) {
            this.source = source;
            this.distances = distances;
            this.previous = previous;
        }

        public long distanceTo(int target) {
            checkVertex(target);
            return distances[target];
        }

        public boolean isReachable(int target) {
            return distanceTo(target) != Long.MAX_VALUE;
        }

        public List<Integer> pathTo(int target) {
            checkVertex(target);

            if (!isReachable(target)) {
                return List.of();
            }

            List<Integer> path = new ArrayList<>();
            int current = target;

            while (current != -1) {
                path.add(current);

                if (current == source) {
                    break;
                }

                current = previous[current];
            }

            Collections.reverse(path);
            return path;
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= distances.length) {
                throw new IllegalArgumentException(
                        "Invalid vertex: " + vertex
                );
            }
        }
    }
}