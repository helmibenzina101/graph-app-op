package com.example.graphapp.strategy.path;

import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.util.PathResult;

public interface ShortestPathStrategy {
    PathResult findShortestPath(Graph graph, Node startNode, Node endNode);
}