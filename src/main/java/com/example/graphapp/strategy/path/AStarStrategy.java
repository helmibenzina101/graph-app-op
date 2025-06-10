package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;

import java.util.*;

public class AStarStrategy implements ShortestPathStrategy {

    private double heuristic(Node from, Node to) {
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        // Ensure heuristic is admissible (never overestimates)
        // If weights are integers, ensure heuristic produces comparable values.
        return Math.sqrt(dx * dx + dy * dy) / 10.0; // Example scaling factor
    }

    @Override
    public PathResult findShortestPath(Graph graph, Node startNode, Node endNode) {
        if (!graph.getNodes().contains(startNode) || !graph.getNodes().contains(endNode)) {
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }

        // openSet: nodes to be evaluated, ordered by fScore
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(node ->
            // Access gScore and calculate fScore on-the-fly for comparison
            // This requires gScore and fScore to be accessible (e.g., member maps)
            getGScoreForNode(node) + heuristic(node, endNode)
        ));

        // closedSet: nodes already evaluated
        Set<Node> closedSet = new HashSet<>();

        // cameFrom: to reconstruct path
        Map<Node, Node> cameFrom = new HashMap<>();

        // gScore: cost from start to node
        gScore = new HashMap<>(); // Made it a member variable for PQ access
        for (Node node : graph.getNodes()) {
            gScore.put(node, Double.POSITIVE_INFINITY);
        }
        gScore.put(startNode, 0.0);

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(endNode)) {
                return reconstructPath(cameFrom, current, gScore.get(endNode));
            }

            closedSet.add(current);

            for (Edge edge : graph.getEdges()) {
                if (!edge.getSource().equals(current)) continue;

                Node neighbor = edge.getTarget();
                if (closedSet.contains(neighbor)) continue;

                double tentativeGScore = gScore.get(current) + edge.getWeight();

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);

                    // If neighbor is in openSet with a higher gScore, update it.
                    // If not in openSet, add it.
                    // Standard Java PQ doesn't support efficient decrease-key.
                    // So, we remove and re-add.
                    boolean inOpenSet = openSet.contains(neighbor);
                    if (inOpenSet) {
                        openSet.remove(neighbor); // To re-prioritize
                    }
                    openSet.add(neighbor); // Add or re-add with new priority
                }
            }
        }
        return new PathResult(null, Double.POSITIVE_INFINITY, 0); // No path found
    }

    // Helper for PriorityQueue comparator to access gScore
    private Map<Node, Double> gScore; // Member variable to store gScores

    private double getGScoreForNode(Node node) {
        return gScore.getOrDefault(node, Double.POSITIVE_INFINITY);
    }

    private PathResult reconstructPath(Map<Node, Node> cameFrom, Node current, double cost) {
        LinkedList<Node> totalPath = new LinkedList<>();
        totalPath.addFirst(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.addFirst(current);
        }
        return new PathResult(new ArrayList<>(totalPath), cost, 0);
    }
}