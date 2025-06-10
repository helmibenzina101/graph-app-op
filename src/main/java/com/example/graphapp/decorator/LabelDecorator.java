package com.example.graphapp.decorator;

import com.example.graphapp.model.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Un décorateur qui ajoute un label supplémentaire à un GraphElementView.
 * Par exemple, afficher l'ID d'un nœud ou une autre information textuelle.
 */
public class LabelDecorator extends GraphElementDecorator {
    private String additionalLabelText;
    private Color labelColor;
    private double offsetX;
    private double offsetY;
    private Font labelFont;

    public LabelDecorator(GraphElementView decoratedElementView, String additionalLabelText, Color labelColor, double offsetX, double offsetY) {
        super(decoratedElementView);
        this.additionalLabelText = additionalLabelText;
        this.labelColor = labelColor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.labelFont = Font.font("Arial", 10); // Police par défaut
    }

    public LabelDecorator(GraphElementView decoratedElementView, String additionalLabelText) {
        this(decoratedElementView, additionalLabelText, Color.DARKSLATEGRAY, 0, -NodeView.RADIUS - 5); // Position par défaut au-dessus du nœud
    }

    public void setLabelFont(Font font) {
        this.labelFont = font;
    }

    public void setAdditionalLabelText(String text) {
        this.additionalLabelText = text;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // 1. Dessiner l'élément décoré (le nœud ou l'arc lui-même)
        super.draw(gc);

        // 2. Dessiner le label supplémentaire
        Object modelElement = decoratedElementView.getModelElement();
        if (modelElement instanceof Node) {
            Node node = (Node) modelElement;
            gc.setFill(this.labelColor);
            gc.setFont(this.labelFont);
            gc.setTextAlign(TextAlignment.CENTER); // Peut être ajusté

            // Calcule la position du label supplémentaire par rapport au centre du nœud
            double labelX = node.getX() + this.offsetX;
            double labelY = node.getY() + this.offsetY;

            gc.fillText(this.additionalLabelText, labelX, labelY);
        }
        // On pourrait aussi ajouter des labels aux arcs, mais c'est moins courant
        // ou cela nécessiterait une logique de positionnement plus complexe.
    }
}