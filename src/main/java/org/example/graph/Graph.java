package org.example.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {

    private final int vertexCount;
    private final boolean directed;
    private final List<List<Edge>> adjacencyList;
    private final List<Edge> edges;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex count cannot be negative"
            );
        }

        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adjacencyList = new ArrayList<>();
        this.edges = new ArrayList<>();

        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        Edge edge = new Edge(from, to, weight);

        adjacencyList.get(from).add(edge);
        edges.add(edge);

        if (!directed && from != to) {
            adjacencyList.get(to).add(new Edge(to, from, weight));
        }
    }

    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);

        return Collections.unmodifiableList(
                adjacencyList.get(vertex)
        );
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean isDirected() {
        return directed;
    }

    public void printGraph() {
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            System.out.print(vertex + ": ");

            for (Edge edge : adjacencyList.get(vertex)) {
                System.out.print(
                        edge.to() + "(" + edge.weight() + ") "
                );
            }

            System.out.println();
        }
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException(
                    "Invalid vertex: " + vertex
            );
        }
    }
}