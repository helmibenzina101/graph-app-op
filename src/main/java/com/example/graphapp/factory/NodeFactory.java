package com.example.graphapp.factory;

import com.example.graphapp.model.Node;

public class NodeFactory { // Peut être fusionné dans GraphElementFactory ou être spécifique
    public Node createNode(String label, double x, double y) {
        return new Node(label, x, y);
    }
}