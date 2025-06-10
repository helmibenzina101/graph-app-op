package com.example.graphapp.decorator;

import com.example.graphapp.model.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class NodeView implements GraphElementView {
    protected Node node;
    public static final double RADIUS = 15;

    public NodeView(Node node) {
        this.node = node;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(node.getX() - RADIUS, node.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(node.getX() - RADIUS, node.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(node.getLabel(), node.getX(), node.getY() + RADIUS / 3);
    }
    
    @Override
    public Node getModelElement() {
        return node;
    }

    @Override
    public boolean contains(double x, double y) {
        double dx = node.getX() - x;
        double dy = node.getY() - y;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }
}