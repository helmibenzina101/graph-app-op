package com.example.graphapp.decorator;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HighlightDecorator extends GraphElementDecorator {
    private Color highlightColor;

    public HighlightDecorator(GraphElementView decoratedElementView, Color highlightColor) {
        super(decoratedElementView);
        this.highlightColor = highlightColor;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc); // Dessine l'élément original d'abord
        Object modelElement = decoratedElementView.getModelElement();

        if (modelElement instanceof Node) {
            Node node = (Node) modelElement;
            gc.setStroke(highlightColor);
            gc.setLineWidth(3);
            gc.strokeOval(node.getX() - NodeView.RADIUS -2, node.getY() - NodeView.RADIUS-2, 2 * NodeView.RADIUS + 4, 2 * NodeView.RADIUS + 4);
        } else if (modelElement instanceof Edge) {
            Edge edge = (Edge) modelElement;
            gc.setStroke(highlightColor);
            gc.setLineWidth(4); // Ligne plus épaisse pour le highlight
            gc.strokeLine(edge.getSource().getX(), edge.getSource().getY(), edge.getTarget().getX(), edge.getTarget().getY());
             // Redessiner la flèche et le poids par-dessus si nécessaire (ou adapter EdgeView)
        }
         gc.setLineWidth(1); // reset
    }
}