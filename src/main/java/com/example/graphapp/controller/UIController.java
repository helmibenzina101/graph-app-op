package com.example.graphapp.controller;

import com.example.graphapp.factory.AlgorithmStrategyFactory;
import com.example.graphapp.factory.EdgeFactory;
import com.example.graphapp.factory.LoggingStrategyFactory;
import com.example.graphapp.factory.NodeFactory;
import com.example.graphapp.model.Edge;
import com.example.graphapp.model.Node;
import com.example.graphapp.singleton.GraphManager;
import com.example.graphapp.strategy.path.ShortestPathStrategy;
import com.example.graphapp.strategy.logging.LoggingStrategy;
import com.example.graphapp.util.PathResult;
import com.example.graphapp.view.GraphCanvas;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.util.Optional;

public class UIController {
    private GraphManager graphManager;
    private GraphCanvas graphCanvas;
    private NodeFactory nodeFactory;
    private EdgeFactory edgeFactory;
    private AlgorithmStrategyFactory algorithmFactory;
    private LoggingStrategyFactory loggingFactory;

    private enum InteractionMode { NODE, EDGE, SELECT_PATH } // Simplifié pour le chemin
    private InteractionMode currentMode = InteractionMode.NODE;

    private Node pendingStartNodeForPath = null; // Pour le mode SELECT_PATH

    public UIController(GraphCanvas canvas) {
        this.graphCanvas = canvas;
        this.graphManager = GraphManager.getInstance();
        this.nodeFactory = new NodeFactory();
        this.edgeFactory = new EdgeFactory();
        this.algorithmFactory = new AlgorithmStrategyFactory();
        this.loggingFactory = new LoggingStrategyFactory();

        LoggingStrategy defaultLoggingStrategy = loggingFactory.createLogger("CONSOLE");
        graphManager.setCurrentLoggingStrategy(defaultLoggingStrategy);
        graphManager.log("UIController: Default logging strategy set to CONSOLE.");

        ShortestPathStrategy defaultPathStrategy = algorithmFactory.createAlgorithm("DIJKSTRA");
        graphManager.setCurrentPathStrategy(defaultPathStrategy);
        graphManager.log("UIController: Default pathfinding strategy set to DIJKSTRA.");
    }

    /**
     * Gère les clics simples sur le canvas.
     *
     * @param event L'événement MouseEvent.
     * @param clickedNode Le nœud cliqué (peut être null).
     * @param clickedEdge L'arc cliqué (peut être null, et est vérifié si clickedNode est null).
     * @param x Coordonnée x du clic.
     * @param y Coordonnée y du clic.
     */
    public void handleCanvasClick(MouseEvent event, Node clickedNode, Edge clickedEdge, double x, double y) {
        String logDetails = "UIController: Canvas click received. Mode: " + currentMode;
        if (clickedNode != null) logDetails += ", Clicked Node: " + clickedNode.getLabel();
        else if (clickedEdge != null) logDetails += ", Clicked Edge: " + clickedEdge;
        else logDetails += ", Clicked on empty space.";
        graphManager.log(logDetails);

        if (event.getButton() == MouseButton.PRIMARY) {
            handlePrimaryClickLogic(clickedNode, clickedEdge, x, y);
        } else if (event.getButton() == MouseButton.SECONDARY) { // Clic droit
            if (clickedNode != null) {
                confirmAndRemoveNode(clickedNode);
            } else if (clickedEdge != null) {
                confirmAndRemoveEdge(clickedEdge);
            }
        }
        graphCanvas.redraw();
    }

    private void confirmAndRemoveNode(Node node) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le nœud '" + node.getLabel() + "' et tous ses arcs connectés ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            graphManager.log("UIController: Removing node " + node.getLabel() + " after confirmation.");
            graphManager.getGraph().removeNode(node); // Supprime le nœud et ses arcs (logique dans Graph.java)
            // Réinitialiser la sélection de chemin si le nœud supprimé était impliqué
            if (node.equals(graphCanvas.getSelectedNodeForPathStart()) || node.equals(pendingStartNodeForPath)) {
                graphCanvas.setSelectedNodeForPathStart(null);
                pendingStartNodeForPath = null;
            }
            if (node.equals(graphCanvas.getSelectedNodeForPathEnd())) {
                graphCanvas.setSelectedNodeForPathEnd(null);
            }
            if(graphCanvas.getSelectedNodeForPathStart() == null || graphCanvas.getSelectedNodeForPathEnd() == null) {
                 graphManager.findShortestPath(null, null); // Effacer le chemin affiché
            }
        } else {
            graphManager.log("UIController: Node removal cancelled for " + node.getLabel());
        }
    }

    private void confirmAndRemoveEdge(Edge edge) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'arc de '" + edge.getSource().getLabel() + "' à '" + edge.getTarget().getLabel() + "' ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            graphManager.log("UIController: Removing edge " + edge + " after confirmation.");
            graphManager.getGraph().removeEdge(edge);
            // Si un chemin était affiché, il faut le recalculer ou l'effacer
            // Pour la simplicité, on peut forcer un recalcul si des nœuds de chemin sont sélectionnés
            if(graphCanvas.getSelectedNodeForPathStart() != null && graphCanvas.getSelectedNodeForPathEnd() != null) {
                 calculateAndDisplayPath();
            } else {
                 graphManager.findShortestPath(null,null); // effacer
            }
        } else {
            graphManager.log("UIController: Edge removal cancelled for " + edge);
        }
    }


    private void handlePrimaryClickLogic(Node clickedNode, Edge clickedEdge, double x, double y) {
        // Ignorer les clics sur les arêtes pour la création, etc. (sauf si on veut un mode "modifier arc")
        if (clickedEdge != null && currentMode != InteractionMode.SELECT_PATH) { // En mode SELECT_PATH, on ignore les arcs
             graphManager.log("UIController: Clicked on an edge in a mode not handling edges. No action.");
             return;
        }

        switch (currentMode) {
            case NODE:
                if (clickedNode == null) {
                    TextInputDialog dialog = new TextInputDialog("N" + (graphManager.getGraph().getNodes().size() + 1));
                    dialog.setTitle("Nom du Nœud");
                    dialog.setHeaderText("Entrez le nom du nouveau nœud :");
                    dialog.setContentText("Nom:");
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(name -> {
                        if (name.trim().isEmpty()) {
                            graphManager.log("UIController: Node creation cancelled, empty name.");
                            Alert alert = new Alert(Alert.AlertType.WARNING, "Le nom du nœud ne peut pas être vide.");
                            alert.showAndWait();
                            return;
                        }
                        Node newNode = nodeFactory.createNode(name, x, y);
                        graphManager.getGraph().addNode(newNode);
                        graphManager.log("UIController: Node created: " + newNode.getLabel());
                    });
                } else {
                    graphManager.log("UIController: Clicked on existing node " + clickedNode.getLabel() + " in NODE mode. No action.");
                }
                break;

            case EDGE:
                if (clickedNode != null) {
                    if (graphCanvas.getFirstNodeForEdge() == null) {
                        graphCanvas.setFirstNodeForEdge(clickedNode);
                        graphManager.log("UIController: First node for edge (click-mode) selected: " + clickedNode.getLabel());
                    } else {
                        Node sourceNode = graphCanvas.getFirstNodeForEdge();
                        if (!sourceNode.equals(clickedNode)) {
                            createEdgeBetweenNodes(sourceNode, clickedNode);
                        } else {
                            graphManager.log("UIController: Cannot create an edge to the same node (click-mode).");
                        }
                        graphCanvas.setFirstNodeForEdge(null);
                    }
                } else {
                    graphCanvas.setFirstNodeForEdge(null);
                    graphManager.log("UIController: Edge creation (click-mode) cancelled by clicking on empty space.");
                }
                break;

            case SELECT_PATH:
                if (clickedNode != null) {
                    if (pendingStartNodeForPath == null) { // Si aucun nœud de départ n'est en attente
                        pendingStartNodeForPath = clickedNode;
                        graphCanvas.setSelectedNodeForPathStart(clickedNode); // Mettre à jour pour le feedback visuel
                        graphCanvas.setSelectedNodeForPathEnd(null); // Effacer la fin précédente
                        graphManager.log("UIController: SELECT_PATH - START node selected: " + clickedNode.getLabel() + ". Select END node.");
                    } else { // Un nœud de départ était en attente, ce clic est pour le nœud de fin
                        if (!clickedNode.equals(pendingStartNodeForPath)) {
                            graphCanvas.setSelectedNodeForPathEnd(clickedNode);
                            graphManager.log("UIController: SELECT_PATH - END node selected: " + clickedNode.getLabel() + ". Calculating path.");
                            calculateAndDisplayPath(); // Calculer et afficher le chemin
                            // Réinitialiser pour la prochaine sélection de chemin
                            pendingStartNodeForPath = null;
                            // Optionnel : revenir à un mode par défaut après calcul ?
                            // setInteractionMode("NODE");
                            // graphCanvas.setSelectedNodeForPathStart(null); // Effacer pour la prochaine fois
                            // graphCanvas.setSelectedNodeForPathEnd(null);
                        } else {
                            graphManager.log("UIController: SELECT_PATH - End node cannot be the same as start node. Start node remains: " + pendingStartNodeForPath.getLabel());
                            // L'utilisateur a recliqué sur le nœud de départ, on ne fait rien, il doit choisir une fin différente.
                        }
                    }
                } else { // Clic sur un espace vide en mode SELECT_PATH
                    graphManager.log("UIController: SELECT_PATH - Clicked on empty space. Resetting path selection.");
                    pendingStartNodeForPath = null;
                    graphCanvas.setSelectedNodeForPathStart(null);
                    graphCanvas.setSelectedNodeForPathEnd(null);
                    graphManager.findShortestPath(null,null); // Efface le chemin affiché
                }
                break;
        }
    }

    private void createEdgeBetweenNodes(Node source, Node target) {
        TextInputDialog weightDialog = new TextInputDialog("1");
        weightDialog.setTitle("Poids de l'Arc");
        weightDialog.setHeaderText("Entrez le poids pour l'arc de " + source.getLabel() + " à " + target.getLabel() + ":");
        weightDialog.setContentText("Poids:");
        Optional<String> weightResult = weightDialog.showAndWait();

        weightResult.ifPresent(weightStr -> {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight < 0 && (graphManager.getCurrentPathStrategy().getClass().getSimpleName().equals("DijkstraStrategy") ||
                                    graphManager.getCurrentPathStrategy().getClass().getSimpleName().equals("AStarStrategy") ||
                                    graphManager.getCurrentPathStrategy().getClass().getSimpleName().equals("BFSStrategy"))) {
                    graphManager.log("UIController: Negative weight (" + weight + ") for Dijkstra/A*/BFS.");
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Poids négatifs et Dijkstra/A*/BFS peuvent donner des résultats incorrects.");
                    alert.showAndWait();
                }
                Edge newEdge = edgeFactory.createEdge(source, target, weight);
                graphManager.getGraph().addEdge(newEdge);
                graphManager.log("UIController: Edge created: " + newEdge);
            } catch (NumberFormatException e) {
                graphManager.log("UIController: Invalid weight: " + weightStr);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Poids invalide.");
                alert.showAndWait();
            }
        });
    }

    public void createEdgeFromDrag(Node sourceNode, Node targetNode) {
        graphManager.log("UIController: createEdgeFromDrag " + sourceNode.getLabel() + " -> " + targetNode.getLabel());
        // La vérification du mode EDGE est maintenant implicitement gérée par GraphCanvas qui n'initie
        // edgeDragSourceNode que si le mode est approprié.
        createEdgeBetweenNodes(sourceNode, targetNode);
        graphCanvas.redraw();
    }

    public void setInteractionMode(String modeName) {
        graphCanvas.setFirstNodeForEdge(null); // Annuler la création d'arc par clic-clic
        pendingStartNodeForPath = null; // Annuler la sélection de chemin en attente

        String upperModeName = modeName.toUpperCase();
        switch (upperModeName) {
            case "NODE":
                currentMode = InteractionMode.NODE;
                break;
            case "EDGE":
                currentMode = InteractionMode.EDGE;
                break;
            case "PATH": // Renommé pour plus de clarté
                currentMode = InteractionMode.SELECT_PATH;
                graphManager.log("UIController: Mode SELECT_PATH: Click a START node, then an END node.");
                // Ne pas effacer la sélection existante ici, l'utilisateur peut vouloir juste changer de mode
                // et conserver sa sélection pour recalculer avec un autre algo.
                // L'effacement se fait si on clique sur le vide en mode SELECT_PATH.
                break;
            default:
                graphManager.log("UIController: Unknown mode '" + modeName + "', defaulting to NODE.");
                currentMode = InteractionMode.NODE;
        }
        graphManager.log("UIController: Interaction mode set to: " + currentMode);
        // Si on quitte le mode PATH, effacer les sélections de chemin pour éviter confusion
        if (!"PATH".equals(upperModeName)) {
            graphCanvas.setSelectedNodeForPathStart(null);
            graphCanvas.setSelectedNodeForPathEnd(null);
            graphManager.findShortestPath(null,null); // Effacer le chemin affiché
        }
        graphCanvas.redraw();
    }

    public String getCurrentModeName() {
        return currentMode.name();
    }

    public void changeAlgorithm(String algoName) {
        try {
            ShortestPathStrategy strategy = algorithmFactory.createAlgorithm(algoName);
            graphManager.setCurrentPathStrategy(strategy);
            if (pendingStartNodeForPath == null && graphCanvas.getSelectedNodeForPathStart() != null && graphCanvas.getSelectedNodeForPathEnd() != null) {
                // Si un chemin complet était déjà sélectionné (pas juste un pendingStartNode)
                calculateAndDisplayPath();
            } else if (pendingStartNodeForPath != null) {
                graphManager.log("UIController: Algorithm changed. Start node " + pendingStartNodeForPath.getLabel() + " is pending for path. Select end node.");
                // Ne rien faire de plus, attendre la sélection du nœud de fin.
                 graphManager.findShortestPath(null,null); // Efface le chemin affiché
            } else {
                 graphManager.findShortestPath(null,null); // Efface le chemin affiché
            }
        } catch (IllegalArgumentException e) {
            graphManager.log("UIController: Error changing algorithm - " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Algorithme inconnu : " + algoName);
            alert.showAndWait();
        }
        graphCanvas.redraw();
    }

    public void changeLoggingStrategy(String strategyName) {
        // ... (code existant, semble correct) ...
        try {
            LoggingStrategy oldStrategy = graphManager.getCurrentLoggingStrategy();
            if (oldStrategy instanceof com.example.graphapp.strategy.logging.DatabaseLoggingStrategy) {
                 ((com.example.graphapp.strategy.logging.DatabaseLoggingStrategy) oldStrategy).closeConnection();
            }
            LoggingStrategy strategy = loggingFactory.createLogger(strategyName);
            graphManager.setCurrentLoggingStrategy(strategy);
        } catch (IllegalArgumentException e) {
            graphManager.log("UIController: Error changing logging strategy - " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Stratégie de log inconnue : " + strategyName);
            alert.showAndWait();
        }
    }

    public void calculateAndDisplayPath() {
        Node start = graphCanvas.getSelectedNodeForPathStart(); // Utiliser ceux-ci pour le calcul final
        Node end = graphCanvas.getSelectedNodeForPathEnd();

        if (start != null && end != null) {
            graphManager.log("UIController: Calculating shortest path from " + start.getLabel() + " to " + end.getLabel() + ".");
            PathResult result = graphManager.findShortestPath(start, end);
            if (result.getPath() == null || result.getPath().isEmpty()) {
                 if(result.getCost() != Double.NEGATIVE_INFINITY) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun chemin trouvé entre " + start.getLabel() + " et " + end.getLabel() + ".");
                    alert.showAndWait();
                 } else {
                     Alert alert = new Alert(Alert.AlertType.ERROR, "Un cycle de poids négatif a été détecté. Impossible de calculer le plus court chemin de manière fiable.");
                     alert.showAndWait();
                 }
            }
        } else {
            graphManager.log("UIController: Path calculation skipped. Start or end node not fully selected.");
            graphManager.findShortestPath(null, null);
            // Pas d'alerte ici, car l'utilisateur peut être en train de sélectionner.
        }
        graphCanvas.redraw();
    }

    public void clearGraph() {
        graphManager.getGraph().clear();
        graphCanvas.setSelectedNodeForPathStart(null);
        graphCanvas.setSelectedNodeForPathEnd(null);
        pendingStartNodeForPath = null;
        graphCanvas.setFirstNodeForEdge(null);
        graphManager.findShortestPath(null, null);
        graphManager.log("UIController: Graph cleared.");
    }
}