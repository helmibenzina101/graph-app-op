package com.example.graphapp.decorator;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface pour les éléments graphiques du graphe qui peuvent être dessinés
 * et potentiellement décorés.
 * Chaque élément visuel (comme une vue de nœud ou d'arc) implémentera cette interface.
 */
public interface GraphElementView {
    /**
     * Méthode pour dessiner l'élément sur le canvas.
     * @param gc Le contexte graphique du canvas JavaFX.
     */
    void draw(GraphicsContext gc);

    /**
     * Retourne l'objet du modèle (Node ou Edge) que cette vue représente.
     * Cela permet de lier la représentation visuelle à l'objet de données sous-jacent.
     * @return L'objet Node ou Edge du modèle.
     */
    Object getModelElement();

    /**
     * Vérifie si les coordonnées (x, y) données sont contenues dans les limites
     * de cet élément graphique.
     * Utile pour la détection de clics ou la sélection.
     * @param x Coordonnée X à vérifier.
     * @param y Coordonnée Y à vérifier.
     * @return true si le point (x, y) est à l'intérieur de l'élément, false sinon.
     */
    boolean contains(double x, double y);
}