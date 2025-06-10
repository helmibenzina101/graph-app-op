package com.example.graphapp.singleton;

import com.example.graphapp.model.Graph;
import com.example.graphapp.model.Node;
import com.example.graphapp.strategy.path.ShortestPathStrategy;
import com.example.graphapp.strategy.logging.LoggingStrategy;
import com.example.graphapp.util.PathResult;
import com.example.graphapp.observer.Observer; // Pour que GraphManager puisse observer le graphe aussi
import com.example.graphapp.observer.Subject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GraphManager implements Observer {
    private static GraphManager instance;
    private Graph graph;
    private ShortestPathStrategy currentPathStrategy;
    private LoggingStrategy currentLoggingStrategy;
    private List<Node> lastCalculatedPath; // Pour l'affichage

    private GraphManager() {
        this.graph = new Graph();
        this.graph.addObserver(this); // Le manager observe le graphe pour recalculer si besoin.
        // Default strategies can be set here or via UI
    }

    public static synchronized GraphManager getInstance() {
        if (instance == null) {
            instance = new GraphManager();
        }
        return instance;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        if (this.graph != null) {
            this.graph.removeObserver(this);
        }
        this.graph = graph;
        this.graph.addObserver(this);
        log("Graph changed.");
        // Potentially trigger observers of GraphManager if UI elements depend on the graph instance itself
    }

    public void setCurrentPathStrategy(ShortestPathStrategy strategy) {
        this.currentPathStrategy = strategy;
        log("Pathfinding strategy changed to: " + strategy.getClass().getSimpleName());
        // Recalculate path if nodes are selected
    }

    public ShortestPathStrategy getCurrentPathStrategy() {
        return currentPathStrategy;
    }
    
    public void setCurrentLoggingStrategy(LoggingStrategy strategy) {
        this.currentLoggingStrategy = strategy;
        log("Logging strategy changed to: " + strategy.getClass().getSimpleName());
    }

    public LoggingStrategy getCurrentLoggingStrategy() {
        return currentLoggingStrategy;
    }
    
    public PathResult findShortestPath(Node start, Node end) {
        if (currentPathStrategy == null) {
            log("Error: No pathfinding strategy selected.");
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }
        if (start == null || end == null) {
            log("Error: Start or end node not selected for pathfinding.");
            return new PathResult(null, Double.POSITIVE_INFINITY, 0);
        }

        log("Calculating shortest path from " + start.getLabel() + " to " + end.getLabel() +
            " using " + currentPathStrategy.getClass().getSimpleName());
        
        long startTime = System.nanoTime();
        PathResult result = currentPathStrategy.findShortestPath(graph, start, end);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // ms

        result.setCalculationTimeMs(duration);
        this.lastCalculatedPath = result.getPath();

        String logMessage = String.format(
            "Path from %s to %s with %s: %s. Cost: %.2f. Time: %d ms. Graph: %d nodes, %d edges.",
            start.getLabel(), end.getLabel(), currentPathStrategy.getClass().getSimpleName(),
            result.getPath() != null ? result.getPath().toString() : "No path found",
            result.getCost(),
            duration,
            graph.getNodes().size(), graph.getEdges().size()
        );
        log(logMessage);
        
        // Notify observers of GraphManager (e.g., UI to update path display)
        // This is an example if GraphManager itself is a Subject for UI updates
        // For simplicity here, the UIController will get this info directly or observe GraphManager.
        return result;
    }
    
    public List<Node> getLastCalculatedPath() {
        return lastCalculatedPath;
    }

    public void log(String message) {
        if (currentLoggingStrategy != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            currentLoggingStrategy.log("[" + timestamp + "] " + message);
        } else {
            System.out.println("[LOG - No Strategy] " + message); // Fallback
        }
    }

    @Override
    public void update(Subject subject, Object arg) {
        if (subject instanceof Graph) {
            log("GraphManager observed change in Graph: " + arg.toString());
            // Potentially trigger recalculation or UI update
            this.lastCalculatedPath = null; // Invalidate last path
            // Notify observers of GraphManager that the graph has changed
        }
    }
}
