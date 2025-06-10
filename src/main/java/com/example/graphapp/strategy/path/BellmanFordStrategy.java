package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;

import java.util.*;
import java.util.stream.Collectors;

public class BellmanFordStrategy implements ShortestPathStrategy {
    @Override
    public PathResult findShortestPath(Graph graph, Node startNode, Node endNode) {
        if (!graph.getNodes().contains(startNode) || !graph.getNodes().contains(endNode)) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();

        for (Node node : nodes) {
            distances.put(node, Double.POSITIVE_INFINITY);
            predecessors.put(node, null);
        }
        distances.put(startNode, 0.0);

        // Relax edges |V|-1 times
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (Edge edge : edges) {
                Node u = edge.getSource();
                Node v = edge.getTarget();
                double weight = edge.getWeight();
                if (distances.get(u) != Double.POSITIVE_INFINITY && distances.get(u) + weight < distances.get(v)) {
                    distances.put(v, distances.get(u) + weight);
                    predecessors.put(v, u);
                }
            }
        }

        // Check for negative-weight cycles
        for (Edge edge : edges) {
            Node u = edge.getSource();
            Node v = edge.getTarget();
            double weight = edge.getWeight();
            if (distances.get(u) != Double.POSITIVE_INFINITY && distances.get(u) + weight < distances.get(v)) {
                // Negative cycle detected
                System.err.println("Graph contains a negative-weight cycle accessible from the source.");
                // Optionally, reconstruct and return the cycle, or just indicate an error.
                // For simplicity, we'll return no path in case of a negative cycle impacting the path to endNode.
                // A full negative cycle detection might be more complex if it doesn't directly impact the path.
                return new PathResult(null, Double.NEGATIVE_INFINITY, 0); // Indicate error or cycle
            }
        }

        if (distances.get(endNode) == Double.POSITIVE_INFINITY) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0); // No path
        }

        LinkedList<Node> path = new LinkedList<>();
        Node current = endNode;
        while (current != null) {
            path.addFirst(current);
            if (current.equals(startNode)) break;
            current = predecessors.get(current);
        }
         // Check if path reconstruction actually reached startNode
        if (path.isEmpty() || !path.getFirst().equals(startNode)) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0); // Should not happen if distance finite and no neg cycle
        }

        return new PathResult(new ArrayList<>(path), distances.get(endNode), 0);
    }
}