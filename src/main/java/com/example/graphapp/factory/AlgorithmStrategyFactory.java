package com.example.graphapp.factory;

import com.example.graphapp.strategy.path.*;

public class AlgorithmStrategyFactory {
    public ShortestPathStrategy createAlgorithm(String type) {
        if (type == null) return null;
        switch (type.toUpperCase()) {
            case "DIJKSTRA":
                return new DijkstraStrategy();
            case "BELLMAN-FORD":
                return new BellmanFordStrategy(); // À créer
            case "FLOYD-WARSHALL":
                return new FloydWarshallStrategy(); // À créer
            case "A*":
                return new AStarStrategy(); // À créer
            case "BFS":
                return new BFSStrategy();
            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + type);
        }
    }
}