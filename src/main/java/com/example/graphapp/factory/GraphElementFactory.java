package com.example.graphapp.factory;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Node;

public interface GraphElementFactory {
    Node createNode(String label, double x, double y);
    Edge createEdge(Node source, Node target, double weight);
}