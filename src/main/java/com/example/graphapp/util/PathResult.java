package com.example.graphapp.util;

import com.example.graphapp.model.Node;
import java.util.List;

public class PathResult {
    private final List<Node> path;
    private final double cost;
    private long calculationTimeMs; // Temps de calcul en millisecondes

    public PathResult(List<Node> path, double cost, long calculationTimeMs) {
        this.path = path;
        this.cost = cost;
        this.calculationTimeMs = calculationTimeMs;
    }

    public List<Node> getPath() {
        return path;
    }

    public double getCost() {
        return cost;
    }

    public long getCalculationTimeMs() {
        return calculationTimeMs;
    }
    
    public void setCalculationTimeMs(long time) { // Permet au GraphManager de le définir après coup
        this.calculationTimeMs = time;
    }

    @Override
    public String toString() {
        return "PathResult{" +
               "path=" + (path != null ? path.stream().map(Node::getLabel).reduce((s1,s2) -> s1+"->"+s2).orElse("N/A") : "None") +
               ", cost=" + cost +
               ", timeMs=" + calculationTimeMs +
               '}';
    }
}