package com.example.graphapp.view;

import com.example.graphapp.controller.UIController;
import com.example.graphapp.singleton.GraphManager;
import com.example.graphapp.strategy.logging.DatabaseLoggingStrategy;
import com.example.graphapp.strategy.logging.LoggingStrategy;

import javafx.application.Application;
// import javafx.application.Platform; // Non utilisé directement ici
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GraphApplication extends Application {

    private GraphCanvas graphCanvas;
    private UIController uiController;
    private TextArea logArea;
    private GraphManager graphManager;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Graph Drawing & Shortest Path");
        graphManager = GraphManager.getInstance();

        BorderPane root = new BorderPane();

        graphCanvas = new GraphCanvas(800, 600);
        uiController = new UIController(graphCanvas);
        graphCanvas.setUIController(uiController);

        // Configuration améliorée du TextArea pour les logs
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(250); // Augmenté à 250 pixels
        logArea.setMinHeight(200); // Hauteur minimale
        logArea.setMaxHeight(Double.MAX_VALUE); // Permet l'expansion si nécessaire
        logArea.setWrapText(true); // Activer le retour à la ligne automatique
        
        // Style amélioré pour meilleure lisibilité
        logArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                         "-fx-font-size: 13px; " +
                         "-fx-background-color: #f8f8f8; " + // Fond légèrement grisé
                         "-fx-border-color: #ddd; " +        // Bordure subtile
                         "-fx-border-width: 1px;");
        
        // Créer un ScrollPane pour garantir le défilement
        ScrollPane scrollPane = new ScrollPane(logArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Toujours afficher la barre de défilement verticale

        VBox controlPanel = createControlPanel();

        root.setTop(controlPanel);
        root.setCenter(graphCanvas);
        root.setBottom(scrollPane); // Utiliser le ScrollPane au lieu du TextArea directement

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        logArea.appendText("Application démarrée. Mode initial: NŒUD.\n");
        graphManager.log("GraphApplication UI fully initialized.");
        
        // Auto-scroll vers le bas quand de nouveaux logs sont ajoutés
        logArea.textProperty().addListener((observable, oldValue, newValue) -> {
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));

        Label modeLabel = new Label("Mode:");
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton nodeMode = new RadioButton("Nœud (Créer/Déplacer)");
        nodeMode.setToggleGroup(modeGroup);
        nodeMode.setSelected(true);
        nodeMode.setOnAction(e -> {
            uiController.setInteractionMode("NODE");
            logArea.appendText("Mode changé en: NŒUD\n");
        });

        RadioButton edgeMode = new RadioButton("Arc (Clic-Clic ou Glisser)");
        edgeMode.setToggleGroup(modeGroup);
        edgeMode.setOnAction(e -> {
            uiController.setInteractionMode("EDGE");
            logArea.appendText("Mode changé en: ARC\n");
        });

        RadioButton pathMode = new RadioButton("Chemin (Clic Départ puis Clic Fin)"); // UX Simplifiée
        pathMode.setToggleGroup(modeGroup);
        pathMode.setOnAction(e -> {
            uiController.setInteractionMode("PATH"); // Le UIController gère la logique SELECT_PATH
            logArea.appendText("Mode changé en: CHEMIN (Sélectionnez nœud départ, puis nœud fin)\n");
        });

        HBox modeBox = new HBox(10, modeLabel, nodeMode, edgeMode, pathMode);
        modeBox.setSpacing(15);


        Label algoLabel = new Label("Algorithme:");
        ComboBox<String> algoComboBox = new ComboBox<>();
        algoComboBox.getItems().addAll("Dijkstra", "Bellman-Ford", "Floyd-Warshall", "A*", "BFS");
        algoComboBox.setValue("Dijkstra");
        algoComboBox.setOnAction(e -> {
            uiController.changeAlgorithm(algoComboBox.getValue());
            logArea.appendText("Algorithme changé en: " + algoComboBox.getValue() + "\n");
        });
        HBox algoBox = new HBox(10, algoLabel, algoComboBox);

        Label logStrategyLabel = new Label("Sauvegarde Logs:");
        ComboBox<String> logStrategyComboBox = new ComboBox<>();
        logStrategyComboBox.getItems().addAll("Console", "File", "Database");
        logStrategyComboBox.setValue("Console");
        logStrategyComboBox.setOnAction(e -> {
            uiController.changeLoggingStrategy(logStrategyComboBox.getValue());
            logArea.appendText("Stratégie de log (GraphManager) changée en: " + logStrategyComboBox.getValue() + "\n");
        });
        HBox logStrategyBox = new HBox(10, logStrategyLabel, logStrategyComboBox);

        Button calculatePathButton = new Button("Recalculer Chemin"); // Peut être utile si on change le graphe après sélection
        calculatePathButton.setTooltip(new Tooltip("Recalcule le chemin avec les nœuds départ/fin actuellement sélectionnés.\nUtile si le graphe ou l'algorithme a changé."));
        calculatePathButton.setOnAction(e -> {
            logArea.appendText("Tentative de recalcul du chemin...\n");
            uiController.calculateAndDisplayPath();
        });

        Button clearGraphButton = new Button("Effacer Graphe");
        clearGraphButton.setOnAction(e -> {
            uiController.clearGraph();
            logArea.appendText("Graphe effacé.\n");
        });
        HBox actionBox = new HBox(10, calculatePathButton, clearGraphButton);

        controlPanel.getChildren().addAll(modeBox, algoBox, logStrategyBox, actionBox);
        return controlPanel;
    }

    @Override
    public void stop() throws Exception {
        if (graphManager != null) {
            graphManager.log("Application stopping...");
            LoggingStrategy currentLogger = graphManager.getCurrentLoggingStrategy();
            if (currentLogger instanceof DatabaseLoggingStrategy) {
                ((DatabaseLoggingStrategy) currentLogger).closeConnection();
            }
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
