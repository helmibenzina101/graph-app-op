package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;

import java.util.*;

public class BFSStrategy implements ShortestPathStrategy {
    @Override
    public PathResult findShortestPath(Graph graph, Node startNode, Node endNode) {
        // BFS considère les poids des arêtes comme 1
        if (!graph.getNodes().contains(startNode) || !graph.getNodes().contains(endNode)) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }

        Queue<Node> queue = new LinkedList<>();
        Map<Node, Node> predecessors = new HashMap<>();
        Map<Node, Integer> distances = new HashMap<>(); // Distance en nombre d'arêtes

        queue.add(startNode);
        predecessors.put(startNode, null);
        distances.put(startNode, 0);

        Node current = null;
        while (!queue.isEmpty()) {
            current = queue.poll();
            if (current.equals(endNode)) {
                break; // Path found
            }

            for (Edge edge : graph.getEdges()) {
                if (edge.getSource().equals(current)) {
                    Node neighbor = edge.getTarget();
                    if (!predecessors.containsKey(neighbor)) { // Si non visité
                        predecessors.put(neighbor, current);
                        distances.put(neighbor, distances.get(current) + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }
        
        if (current == null || !current.equals(endNode)) {
             return new PathResult(null, Double.POSITIVE_INFINITY, 0); // Pas de chemin
        }


        List<Node> path = new LinkedList<>();
        Node step = endNode;
        while (step != null) {
            path.add(0, step);
            step = predecessors.get(step);
        }
        
        return new PathResult(path, (double) distances.get(endNode), 0);
    }
}