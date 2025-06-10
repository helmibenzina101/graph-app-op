package com.example.graphapp.decorator;

import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.awt.geom.Line2D;
import javafx.scene.text.TextAlignment;

public class EdgeView implements GraphElementView {
    protected Edge edge;
    public static final double HIT_DETECTION_WIDTH = 8.0; // Largeur pour la détection de clic

    public EdgeView(Edge edge) {
        this.edge = edge;
    }

    @Override
    public void draw(GraphicsContext gc) {
        Node sourceNode = edge.getSource();
        Node targetNode = edge.getTarget();

        double startX = sourceNode.getX();
        double startY = sourceNode.getY();
        double endX = targetNode.getX();
        double endY = targetNode.getY();

        // Calculer l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);
        
        // Rayon du nœud pour ajuster les points de départ et d'arrivée
        double nodeRadius = NodeView.RADIUS;
        
        // Ajuster les points de départ et d'arrivée pour qu'ils commencent et terminent aux bords des nœuds
        double adjustedStartX = startX + nodeRadius * Math.cos(angle);
        double adjustedStartY = startY + nodeRadius * Math.sin(angle);
        double adjustedEndX = endX - nodeRadius * Math.cos(angle);
        double adjustedEndY = endY - nodeRadius * Math.sin(angle);

        // Dessiner la ligne avec les points ajustés
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeLine(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY);

        // Dessiner une flèche plus visible
        double arrowSize = 12; // Taille augmentée
        
        // Créer une flèche plus visible à l'extrémité de la ligne
        gc.setFill(Color.GRAY);
        double[] arrowX = {
            adjustedEndX,
            adjustedEndX - arrowSize * Math.cos(angle - Math.PI/6),
            adjustedEndX - arrowSize * Math.cos(angle + Math.PI/6)
        };
        double[] arrowY = {
            adjustedEndY,
            adjustedEndY - arrowSize * Math.sin(angle - Math.PI/6),
            adjustedEndY - arrowSize * Math.sin(angle + Math.PI/6)
        };
        gc.fillPolygon(arrowX, arrowY, 3);

        // Afficher le poids avec un fond blanc pour meilleure lisibilité
        double textX = (adjustedStartX + adjustedEndX) / 2;
        double textY = (adjustedStartY + adjustedEndY) / 2 - 8;
        String weightText = String.format("%.1f", edge.getWeight());
        
        // Ajouter un fond blanc sous le texte pour meilleure lisibilité
        gc.setFill(Color.WHITE);
        double textWidth = weightText.length() * 7; // Estimation approximative de la largeur
        double textHeight = 16;
        gc.fillRect(textX - textWidth/2, textY - textHeight/2, textWidth, textHeight);
        
        // Dessiner le texte du poids
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(weightText, textX, textY + 4);
    }

    @Override
    public Edge getModelElement() {
        return edge;
    }

    /**
     * Vérifie si le point (x, y) est proche du segment de ligne représentant l'arc.
     * Utilise la distance d'un point à un segment de ligne.
     */
    @Override
    public boolean contains(double x, double y) {
        Node sourceNode = edge.getSource();
        Node targetNode = edge.getTarget();

        double x1 = sourceNode.getX();
        double y1 = sourceNode.getY();
        double x2 = targetNode.getX();
        double y2 = targetNode.getY();
        // Utiliser Line2D.ptSegDistSq qui calcule la distance au carré d'un point à un segment
        // C'est plus efficace car on évite une racine carrée si on compare au carré de la distance de tolérance
        double distSq = Line2D.ptSegDistSq(x1, y1, x2, y2, x, y);

        return distSq <= HIT_DETECTION_WIDTH * HIT_DETECTION_WIDTH;
    }
}
