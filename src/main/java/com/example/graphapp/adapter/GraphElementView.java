package com.example.graphapp.adapter;

import javafx.scene.canvas.GraphicsContext;

// Interface pour les éléments graphiques qui peuvent être dessinés et décorés
public interface GraphElementView {
    void draw(GraphicsContext gc);
    Object getModelElement(); // Pour lier la vue au modèle (Node ou Edge)
    boolean contains(double x, double y); // Pour la sélection
}