package com.example.graphapp.factory;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Node;

public class EdgeFactory { // Peut être fusionné dans GraphElementFactory ou être spécifique
    public Edge createEdge(Node source, Node target, double weight) {
        return new Edge(source, target, weight);
    }
}