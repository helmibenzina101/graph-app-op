package com.example.graphapp.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    private String label;
    private double x; // Pour la position dans l'UI
    private double y; // Pour la position dans l'UI

    public Node(String label, double x, double y) {
        this.id = idCounter.incrementAndGet();
        this.label = label;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
               "id=" + id +
               ", label='" + label + '\'' +
               '}';
    }
}