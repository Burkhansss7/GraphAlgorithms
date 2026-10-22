package org.example;

import org.example.connectivity.SCC;
import org.example.connectivity.TopologicalSort;
import org.example.graph.Edge;
import org.example.graph.Graph;
import org.example.mst.Kruskal;
import org.example.mst.Prim;
import org.example.shortestpath.Dijkstra;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmsTest {

    @Test
    void mstAlgorithmsReturnExpectedWeight() {
        Graph graph = new Graph(5, false);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 5);
        graph.addEdge(2, 3, 8);
        graph.addEdge(3, 4, 3);

        List<Edge> kruskal = Kruskal.findMst(graph);
        List<Edge> prim = Prim.findMst(graph);

        assertEquals(4, kruskal.size());
        assertEquals(4, prim.size());

        assertEquals(11L, totalWeight(kruskal));
        assertEquals(11L, totalWeight(prim));
    }

    @Test
    void mstAlgorithmsRejectDisconnectedGraph() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kruskal.findMst(graph)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Prim.findMst(graph)
        );
    }

    @Test
    void mstAlgorithmsHandleMultipleValidTreesAndEqualWeights() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);

        List<Edge> kruskal = Kruskal.findMst(graph);
        List<Edge> prim = Prim.findMst(graph);

        assertEquals(3, kruskal.size());
        assertEquals(3, prim.size());

        assertEquals(3L, totalWeight(kruskal));
        assertEquals(3L, totalWeight(prim));
    }

    @Test
    void algorithmsHandleSingleVertex() {
        Graph undirected = new Graph(1, false);

        assertTrue(Kruskal.findMst(undirected).isEmpty());
        assertTrue(Prim.findMst(undirected).isEmpty());

        Graph directed = new Graph(1, true);

        assertEquals(List.of(0), TopologicalSort.sort(directed));
        assertEquals(List.of(List.of(0)), SCC.findComponents(directed));

        Dijkstra.Result result = Dijkstra.shortestPaths(directed, 0);

        assertEquals(0L, result.distanceTo(0));
        assertEquals(List.of(0), result.pathTo(0));
    }

    @Test
    void topologicalSortRespectsEveryEdge() {
        Graph graph = new Graph(7, true);

        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 4, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1);

        List<Integer> order = TopologicalSort.sort(graph);

        assertEquals(7, order.size());
        assertEquals(7, new HashSet<>(order).size());

        for (int vertex = 0; vertex < 7; vertex++) {
            assertTrue(order.contains(vertex));
        }

        for (Edge edge : graph.getEdges()) {
            assertTrue(
                    order.indexOf(edge.from()) < order.indexOf(edge.to()),
                    "Invalid order for edge: " + edge
            );
        }
    }

    @Test
    void topologicalSortRejectsCycle() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> TopologicalSort.sort(graph)
        );
    }

    @Test
    void sccFindsExpectedComponentsAndIncludesEachVertexOnce() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(4, 5, 1);

        List<List<Integer>> components = SCC.findComponents(graph);

        Set<Set<Integer>> actual = new HashSet<>();
        Set<Integer> seen = new HashSet<>();

        for (List<Integer> component : components) {
            assertFalse(component.isEmpty());

            actual.add(new HashSet<>(component));

            for (int vertex : component) {
                assertTrue(
                        seen.add(vertex),
                        "Vertex appears more than once: " + vertex
                );
            }
        }

        Set<Set<Integer>> expected = Set.of(
                Set.of(0, 1, 2),
                Set.of(3, 4),
                Set.of(5)
        );

        assertEquals(expected, actual);
        assertEquals(Set.of(0, 1, 2, 3, 4, 5), seen);
    }

    @Test
    void sccHandlesIsolatedVertices() {
        Graph graph = new Graph(3, true);

        List<List<Integer>> components = SCC.findComponents(graph);

        Set<Set<Integer>> actual = new HashSet<>();

        for (List<Integer> component : components) {
            actual.add(new HashSet<>(component));
        }

        assertEquals(
                Set.of(Set.of(0), Set.of(1), Set.of(2)),
                actual
        );
    }

    @Test
    void dijkstraReturnsCorrectDistancesAndPath() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);
        graph.addEdge(3, 4, 3);
        graph.addEdge(4, 5, 1);

        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);

        long[] expected = {0, 3, 1, 4, 7, 8};

        for (int vertex = 0; vertex < expected.length; vertex++) {
            assertEquals(
                    expected[vertex],
                    result.distanceTo(vertex),
                    "Wrong distance to vertex " + vertex
            );
        }

        assertEquals(
                List.of(0, 2, 1, 3, 4, 5),
                result.pathTo(5)
        );
    }

    @Test
    void dijkstraHandlesUnreachableVertex() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 5);

        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);

        assertFalse(result.isReachable(2));
        assertEquals(Long.MAX_VALUE, result.distanceTo(2));
        assertTrue(result.pathTo(2).isEmpty());
    }

    @Test
    void dijkstraRejectsNegativeWeights() {
        Graph graph = new Graph(2, true);

        graph.addEdge(0, 1, -2);

        assertThrows(
                IllegalArgumentException.class,
                () -> Dijkstra.shortestPaths(graph, 0)
        );
    }

    @Test
    void dijkstraHandlesZeroWeightsAndUndirectedEdges() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, 0);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 5);

        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 2);

        assertEquals(2L, result.distanceTo(0));
        assertEquals(List.of(2, 1, 0), result.pathTo(0));
    }

    private long totalWeight(List<Edge> edges) {
        long total = 0;

        for (Edge edge : edges) {
            total += edge.weight();
        }

        return total;
    }
    @Test
    void dsuMergesComponentsAndDetectsExistingConnections() {
        org.example.dsu.DSU dsu = new org.example.dsu.DSU(5);

        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));

        assertEquals(dsu.find(0), dsu.find(2));
        assertNotEquals(dsu.find(0), dsu.find(3));

        assertFalse(dsu.union(0, 2));

        assertTrue(dsu.union(3, 4));
        assertTrue(dsu.union(2, 4));

        assertEquals(dsu.find(0), dsu.find(3));
    }
}