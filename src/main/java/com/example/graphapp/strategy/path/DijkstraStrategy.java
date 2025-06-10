package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;
import java.util.*;

public class DijkstraStrategy implements ShortestPathStrategy {
    @Override
    public PathResult findShortestPath(Graph graph, Node startNode, Node endNode) {
        // Implémentation de Dijkstra (squelette)
        // Vérifier que startNode et endNode existent dans le graph
        if (!graph.getNodes().contains(startNode) || !graph.getNodes().contains(endNode)) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(startNode, 0.0);
        priorityQueue.add(startNode);

        while (!priorityQueue.isEmpty()) {
            Node u = priorityQueue.poll();

            if (u.equals(endNode)) break; // Path found

            graph.getEdges().stream()
                .filter(edge -> edge.getSource().equals(u))
                .forEach(edge -> {
                    Node v = edge.getTarget();
                    double weight = edge.getWeight();
                    double distanceThroughU = distances.get(u) + weight;
                    if (distanceThroughU < distances.get(v)) {
                        distances.put(v, distanceThroughU);
                        predecessors.put(v, u);
                        priorityQueue.remove(v); // Mettre à jour la priorité
                        priorityQueue.add(v);
                    }
                });
        }

        if (distances.get(endNode) == Double.POSITIVE_INFINITY) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0); // Pas de chemin
        }

        List<Node> path = new LinkedList<>();
        Node current = endNode;
        while (current != null) {
            path.add(0, current);
            if (current.equals(startNode)) break;
            current = predecessors.get(current);
        }
        
        return new PathResult(path, distances.get(endNode), 0); // Time will be set by GraphManager
    }
}