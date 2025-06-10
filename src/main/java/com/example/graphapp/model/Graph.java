package com.example.graphapp.model;

import com.example.graphapp.observer.Subject;
import com.example.graphapp.observer.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Graph implements Subject {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final List<Observer> observers;

    public Graph() {
        this.nodes = new CopyOnWriteArrayList<>(); // Thread-safe for modifications during iteration
        this.edges = new CopyOnWriteArrayList<>();
        this.observers = new ArrayList<>();
    }

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            notifyObservers("Node added: " + node.getLabel());
        }
    }

    public void removeNode(Node node) {
        if (nodes.remove(node)) {
            edges.removeIf(edge -> edge.getSource().equals(node) || edge.getTarget().equals(node));
            notifyObservers("Node removed: " + node.getLabel());
        }
    }

    public void addEdge(Edge edge) {
        if (nodes.contains(edge.getSource()) && nodes.contains(edge.getTarget()) && !edges.contains(edge)) {
            edges.add(edge);
            notifyObservers("Edge added: " + edge);
        }
    }

    public void removeEdge(Edge edge) {
        if (edges.remove(edge)) {
            notifyObservers("Edge removed: " + edge);
        }
    }
    
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        notifyObservers("Graph cleared");
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object arg) {
        for (Observer observer : observers) {
            observer.update(this, arg);
        }
    }
}