package com.example.graphapp.view;

import com.example.graphapp.controller.UIController;
import com.example.graphapp.decorator.*;
import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.observer.Observer;
import com.example.graphapp.observer.Subject;
import com.example.graphapp.singleton.GraphManager;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors; // Pas utilisé directement ici

public class GraphCanvas extends Canvas implements Observer {
    private GraphManager graphManager;
    private List<GraphElementView> elementViews; // Pas directement utilisé pour le dessin final, mais pourrait l'être

    // Pour le déplacement de nœud
    private Node draggedNode = null;
    private double dragOffsetX, dragOffsetY;

    // Pour la création d'arêtes par glisser-déposer
    private Node edgeDragSourceNode = null;
    private double currentMouseX, currentMouseY;

    private boolean wasDragged = false;
    private UIController uiController;

    public GraphCanvas(double width, double height) {
        super(width, height);
        this.graphManager = GraphManager.getInstance();
        this.graphManager.getGraph().addObserver(this);
        this.elementViews = new ArrayList<>(); // Peut être utilisé pour stocker les vues décorées si besoin plus tard

        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.setOnMouseMoved(this::handleMouseMoved);

        redraw();
    }

    public void setUIController(UIController controller) {
        this.uiController = controller;
    }

    private void handleMousePressed(MouseEvent event) {
        currentMouseX = event.getX();
        currentMouseY = event.getY();
        Node clickedNode = getNodeAt(currentMouseX, currentMouseY);
        wasDragged = false;

        if (event.getButton() == MouseButton.PRIMARY) {
            if (clickedNode != null) {
                if (uiController != null && "EDGE".equals(uiController.getCurrentModeName()) && this.getFirstNodeForEdge() == null) {
                     // Si en mode EDGE et aucun premier noeud pour l'arc n'est sélectionné par clic, on initie un drag d'arc.
                    // Si un premier noeud EST sélectionné (firstNodeForEdge != null), alors le prochain clic primaire sur un noeud
                    // devrait compléter l'arc via handleCanvasClick, et non initier un drag.
                    edgeDragSourceNode = clickedNode;
                    graphManager.log("Canvas: Started dragging edge from: " + clickedNode.getLabel());
                } else if (uiController == null || !"EDGE".equals(uiController.getCurrentModeName()) || this.getFirstNodeForEdge() != null) {
                    // Si on n'est pas en mode EDGE pour initier un drag d'arc
                    // OU si on est en mode EDGE mais qu'un premier nœud a déjà été cliqué (donc le prochain clic est pour compléter l'arc)
                    // alors on initie un drag de nœud.
                    // Ceci évite qu'un drag de nœud commence si on est en train de faire un deuxième clic pour un arc.
                    // La condition exacte pour 'pas un drag d'arc' :
                    // Soit on n'est pas en mode EDGE, soit on est en mode EDGE mais le premier noeud d'un arc par clic-clic est déjà défini
                    // (auquel cas un clic sur un autre noeud termine l'arc, et un clic sur un noeud pour drag n'est pas l'intention).
                    // Pour simplifier: si on n'initie pas de drag d'arc, on initie un drag de noeud si on clique sur un noeud.
                    if(!(uiController != null && "EDGE".equals(uiController.getCurrentModeName()) && this.getFirstNodeForEdge() == null)) {
                         draggedNode = clickedNode;
                         dragOffsetX = currentMouseX - clickedNode.getX();
                         dragOffsetY = currentMouseY - clickedNode.getY();
                         graphManager.log("Canvas: Started dragging node: " + clickedNode.getLabel());
                    }
                }
            }
        }
        redraw();
    }

    private void handleMouseDragged(MouseEvent event) {
        currentMouseX = event.getX();
        currentMouseY = event.getY();

        if (draggedNode != null) {
            wasDragged = true;
            draggedNode.setX(currentMouseX - dragOffsetX);
            draggedNode.setY(currentMouseY - dragOffsetY);
            redraw();
        } else if (edgeDragSourceNode != null) {
            wasDragged = true;
            redraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        currentMouseX = event.getX();
        currentMouseY = event.getY();

        boolean wasNodeDragAction = (draggedNode != null);
        boolean wasEdgeDragAction = (edgeDragSourceNode != null);

        if (draggedNode != null) {
            graphManager.log("Canvas: Finished dragging node: " + draggedNode.getLabel() + " to (" + String.format("%.1f",draggedNode.getX()) + "," + String.format("%.1f",draggedNode.getY()) + ")");
            draggedNode = null;
        }

        if (edgeDragSourceNode != null) {
            Node targetNode = getNodeAt(currentMouseX, currentMouseY);
            if (targetNode != null && !targetNode.equals(edgeDragSourceNode)) {
                if (uiController != null) {
                    uiController.createEdgeFromDrag(edgeDragSourceNode, targetNode);
                }
            } else {
                graphManager.log("Canvas: Edge drag cancelled or target is source/invalid.");
            }
            edgeDragSourceNode = null;
        }

        if (!wasDragged && uiController != null) {
            Node clickedNode = getNodeAt(currentMouseX, currentMouseY);
            Edge clickedEdge = null;
            if (clickedNode == null) { // Si on n'a pas cliqué sur un noeud, vérifier si on a cliqué sur un arc
                clickedEdge = getEdgeAt(currentMouseX, currentMouseY);
            }
            // Passer clickedNode OU clickedEdge au UIController.
            // UIController devra gérer le cas où l'un des deux est non nul.
            uiController.handleCanvasClick(event, clickedNode, clickedEdge, currentMouseX, currentMouseY);
        }
        redraw();
    }

    private void handleMouseMoved(MouseEvent event) {
        if (edgeDragSourceNode != null) {
            currentMouseX = event.getX();
            currentMouseY = event.getY();
            redraw();
        }
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        Graph graph = graphManager.getGraph();
        List<Node> highlightedPathNodes = graphManager.getLastCalculatedPath();

        List<GraphElementView> viewsToDraw = new ArrayList<>();

        // Arcs
        for (Edge edge : graph.getEdges()) {
            GraphElementView edgeView = new EdgeView(edge);
            if (highlightedPathNodes != null && highlightedPathNodes.size() > 1) {
                for (int i = 0; i < highlightedPathNodes.size() - 1; i++) {
                    if (edge.getSource().equals(highlightedPathNodes.get(i)) &&
                        edge.getTarget().equals(highlightedPathNodes.get(i + 1))) {
                        // Appliquer le décorateur pour le chemin sur l'arc
                        edgeView = new HighlightDecorator(edgeView, Color.ORANGERED);
                        break;
                    }
                }
            }
            viewsToDraw.add(edgeView);
        }

        // Noeuds
        for (Node node : graph.getNodes()) {
            GraphElementView nodeView = new NodeView(node); // Vue de base

            // Décorateur pour le chemin
            if (highlightedPathNodes != null && highlightedPathNodes.contains(node)) {
                nodeView = new HighlightDecorator(nodeView, Color.LAWNGREEN);
            }

            // Décorateur pour le nœud de DÉPART sélectionné pour le calcul de chemin
            if (node.equals(getSelectedNodeForPathStart())) {
                // Le décorateur HighlightDecorator peut être appliqué plusieurs fois si nécessaire,
                // ou nous pouvons créer un MultiDecorator. Pour l'instant, le dernier appliqué "gagne"
                // ou ils se superposent. Idéalement, on voudrait combiner les effets.
                // Une solution simple est d'avoir des couleurs distinctes ou une logique de priorité.
                // Ici, on va appliquer séquentiellement.
                nodeView = new HighlightDecorator(nodeView, Color.DEEPSKYBLUE);
            }
            // Décorateur pour le nœud de FIN sélectionné
            if (node.equals(getSelectedNodeForPathEnd())) {
                nodeView = new HighlightDecorator(nodeView, Color.TOMATO);
            }
            // Décorateur pour le premier nœud sélectionné lors de la création d'un arc (mode clic-clic)
            if (node.equals(firstNodeForEdge)) {
                 nodeView = new HighlightDecorator(nodeView, Color.LIGHTPINK);
            }

            viewsToDraw.add(nodeView);
        }

        // Dessiner les arcs en premier
        viewsToDraw.stream()
            .filter(ev -> ev.getModelElement() instanceof Edge)
            .forEach(ev -> ev.draw(gc));

        // Dessiner les nœuds par-dessus
        viewsToDraw.stream()
            .filter(ev -> ev.getModelElement() instanceof Node)
            .forEach(ev -> ev.draw(gc));

        if (edgeDragSourceNode != null) {
            gc.setStroke(Color.BLUEVIOLET);
            gc.setLineWidth(2);
            gc.setLineDashes(5);
            gc.strokeLine(edgeDragSourceNode.getX(), edgeDragSourceNode.getY(), currentMouseX, currentMouseY);
            gc.setLineDashes(0);
        }
    }

    public Node getNodeAt(double x, double y) {
        List<Node> nodes = graphManager.getGraph().getNodes();
        // Parcourir en ordre inverse pour que les nœuds dessinés en dernier (au-dessus) soient détectés en premier
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            NodeView nv = new NodeView(node);
            if (nv.contains(x, y)) {
                return node;
            }
        }
        return null;
    }

    public Edge getEdgeAt(double x, double y) {
        List<Edge> edges = graphManager.getGraph().getEdges();
        // Parcourir en ordre inverse pour une logique similaire aux nœuds si le z-order importait
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            EdgeView ev = new EdgeView(edge);
            if (ev.contains(x, y)) {
                return edge;
            }
        }
        return null;
    }


    @Override
    public void update(Subject subject, Object arg) {
        if (subject instanceof Graph) {
            Platform.runLater(this::redraw);
        }
    }

    private Node firstNodeForEdge = null;
    private Node selectedNodeForPathStart = null;
    private Node selectedNodeForPathEnd = null;

    public Node getFirstNodeForEdge() { return firstNodeForEdge; }
    public void setFirstNodeForEdge(Node node) {
        this.firstNodeForEdge = node;
        redraw();
    }

    public Node getSelectedNodeForPathStart() { return selectedNodeForPathStart; }
    public void setSelectedNodeForPathStart(Node node) {
        this.selectedNodeForPathStart = node;
        redraw();
    }

    public Node getSelectedNodeForPathEnd() { return selectedNodeForPathEnd; }
    public void setSelectedNodeForPathEnd(Node node) {
        this.selectedNodeForPathEnd = node;
        redraw();
    }
}