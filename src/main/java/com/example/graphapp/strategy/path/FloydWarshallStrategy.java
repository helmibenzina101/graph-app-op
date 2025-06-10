package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;

import java.util.*;

public class FloydWarshallStrategy implements ShortestPathStrategy {

    private Map<Node, Map<Node, Double>> dist;
    private Map<Node, Map<Node, Node>> next;
    private Graph lastProcessedGraph; // To detect if graph changed

    public FloydWarshallStrategy() {
        dist = new HashMap<>();
        next = new HashMap<>();
        lastProcessedGraph = null;
    }

    private void initializeAndCompute(Graph graph) {
        dist.clear();
        next.clear();
        List<Node> nodes = new ArrayList<>(graph.getNodes()); // Ensure consistent ordering for matrix-like access
        int numNodes = nodes.size();

        // Create a mapping from Node object to its index in the 'nodes' list
        Map<Node, Integer> nodeToIndex = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            nodeToIndex.put(nodes.get(i), i);
        }

        // Initialize dist and next matrices represented by maps
        for (Node u : nodes) {
            dist.put(u, new HashMap<>());
            next.put(u, new HashMap<>());
            for (Node v : nodes) {
                dist.get(u).put(v, Double.POSITIVE_INFINITY);
                next.get(u).put(v, null);
            }
            dist.get(u).put(u, 0.0); // Distance to self is 0
        }

        for (Edge edge : graph.getEdges()) {
            Node u = edge.getSource();
            Node v = edge.getTarget();
            if (dist.containsKey(u) && dist.get(u).containsKey(v)) { // Ensure nodes are in our list
                 // Handle parallel edges by taking the minimum weight if strategy implies simple graph
                if (edge.getWeight() < dist.get(u).get(v)) {
                    dist.get(u).put(v, edge.getWeight());
                    next.get(u).put(v, v); // The next node on the path from u to v is v itself initially
                }
            }
        }

        // Floyd-Warshall algorithm
        for (Node k : nodes) {
            for (Node i : nodes) {
                for (Node j : nodes) {
                    if (dist.get(i).get(k) != Double.POSITIVE_INFINITY &&
                        dist.get(k).get(j) != Double.POSITIVE_INFINITY &&
                        dist.get(i).get(k) + dist.get(k).get(j) < dist.get(i).get(j)) {
                        dist.get(i).put(j, dist.get(i).get(k) + dist.get(k).get(j));
                        next.get(i).put(j, next.get(i).get(k)); // Path from i to j goes through k's path
                    }
                }
            }
        }
        this.lastProcessedGraph = graph; // Mark this graph as processed
    }


    @Override
    public PathResult findShortestPath(Graph graph, Node startNode, Node endNode) {
        // Recompute if the graph instance has changed or if not initialized
        // A more robust check would be a hash of the graph structure if Graph objects are mutable
        // For now, we assume if the graph object reference is different, or nodes/edges count changed, recompute.
        boolean recompute = (lastProcessedGraph == null || 
                             lastProcessedGraph != graph || 
                             lastProcessedGraph.getNodes().size() != graph.getNodes().size() || 
                             lastProcessedGraph.getEdges().size() != graph.getEdges().size());
        
        if (recompute) {
             // A simple deep equality check for graph would be better
            // if graph content can change without changing graph object reference
            initializeAndCompute(graph);
        }


        if (!dist.containsKey(startNode) || !dist.get(startNode).containsKey(endNode) ||
            dist.get(startNode).get(endNode) == Double.POSITIVE_INFINITY) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0); // No path
        }

        // Reconstruct path
        List<Node> path = new ArrayList<>();
        Node u = startNode;
        Node v = endNode;

        if (next.get(u) == null || next.get(u).get(v) == null) { // No path or u == v
            if (u.equals(v)) path.add(u);
            else return new PathResult(null, Double.POSITIVE_INFINITY, 0); // No path
        } else {
            path.add(u);
            while (!u.equals(v)) {
                if (next.get(u) == null || next.get(u).get(v) == null) {
                    // Should indicate an issue if dist was finite, means graph structure problem
                    return new PathResult(null, Double.POSITIVE_INFINITY, 0); // Path broken
                }
                u = next.get(u).get(v);
                if (u == null) return new PathResult(null, Double.POSITIVE_INFINITY, 0); // Path broken
                path.add(u);
                if (path.size() > graph.getNodes().size()) { // Cycle or error in path reconstruction
                     System.err.println("Floyd-Warshall: Path reconstruction error or cycle detected where not expected.");
                     return new PathResult(null, Double.NEGATIVE_INFINITY, 0); // Indicate error
                }
            }
        }
        
        return new PathResult(path, dist.get(startNode).get(endNode), 0);
    }
}