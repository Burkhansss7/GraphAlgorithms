package org.example.dsu;

import org.example.metrics.Metrics;

public class DSU {

    private final int[] parent;
    private final int[] rank;
    private final Metrics metrics;

    public DSU(int size) {
        this(size, new Metrics());
    }

    public DSU(int size, Metrics metrics) {
        if (size < 0) {
            throw new IllegalArgumentException(
                    "Size cannot be negative"
            );
        }

        this.parent = new int[size];
        this.rank = new int[size];
        this.metrics = metrics;

        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int vertex) {
        metrics.findCalls++;

        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent[vertex]);
        }

        return parent[vertex];
    }

    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return false;
        }

        metrics.unionCalls++;

        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }

        return true;
    }
}