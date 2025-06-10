# Graph Application
 mvn exec:java -Dexec.mainClass="com.example.graphapp.Main"
A JavaFX application for creating, visualizing, and analyzing graphs with shortest path algorithms.

## Project Structure

### Main Application
- `src/main/java/com/example/graphapp/Main.java`: Entry point that launches the JavaFX application.
- `src/main/java/com/example/graphapp/view/GraphApplication.java`: Main JavaFX application class that sets up the UI.
- `src/main/java/com/example/graphapp/view/GraphCanvas.java`: Canvas for drawing and interacting with the graph.

### Model
- `src/main/java/com/example/graphapp/model/Graph.java`: Core graph data structure implementing the Subject interface.
- `src/main/java/com/example/graphapp/model/Node.java`: Represents a node/vertex in the graph.
- `src/main/java/com/example/graphapp/model/Edge.java`: Represents an edge/connection between nodes.

### Controller
- `src/main/java/com/example/graphapp/controller/UIController.java`: Handles user interactions and updates the model.

### Design Patterns
- **Singleton**: `src/main/java/com/example/graphapp/singleton/GraphManager.java`: Manages the graph instance.
- **Factory**:
  - `src/main/java/com/example/graphapp/factory/NodeFactory.java`: Creates node instances.
  - `src/main/java/com/example/graphapp/factory/EdgeFactory.java`: Creates edge instances.
  - `src/main/java/com/example/graphapp/factory/AlgorithmStrategyFactory.java`: Creates algorithm strategy instances.
  - `src/main/java/com/example/graphapp/factory/LoggingStrategyFactory.java`: Creates logging strategy instances.
- **Strategy**:
  - `src/main/java/com/example/graphapp/strategy/path/ShortestPathStrategy.java`: Interface for path-finding algorithms.
  - `src/main/java/com/example/graphapp/strategy/path/DijkstraStrategy.java`: Dijkstra's algorithm implementation.
  - `src/main/java/com/example/graphapp/strategy/path/BellmanFordStrategy.java`: Bellman-Ford algorithm implementation.
  - `src/main/java/com/example/graphapp/strategy/path/FloydWarshallStrategy.java`: Floyd-Warshall algorithm implementation.
  - `src/main/java/com/example/graphapp/strategy/path/AStarStrategy.java`: A* algorithm implementation.
  - `src/main/java/com/example/graphapp/strategy/path/BFSStrategy.java`: Breadth-First Search implementation.
- **Observer**:
  - `src/main/java/com/example/graphapp/observer/Subject.java`: Interface for observable objects.
  - `src/main/java/com/example/graphapp/observer/Observer.java`: Interface for observer objects.
  - **Implementation Details**:
    - `Graph` implements `Subject` to notify observers when nodes or edges change
    - `GraphManager` implements `Observer` to monitor graph changes and invalidate cached paths
    - `GraphCanvas` implements `Observer` to automatically redraw the visualization when the graph changes
- **Decorator**:
  - `src/main/java/com/example/graphapp/decorator/GraphElementView.java`: Interface for drawable graph elements.
  - `src/main/java/com/example/graphapp/decorator/NodeView.java`: Visual representation of nodes.
  - `src/main/java/com/example/graphapp/decorator/EdgeView.java`: Visual representation of edges.
  - `src/main/java/com/example/graphapp/decorator/GraphElementDecorator.java`: Base decorator class.
  - `src/main/java/com/example/graphapp/decorator/LabelDecorator.java`: Adds labels to graph elements.
- **Adapter**:
  - `src/main/java/com/example/graphapp/adapter/ExternalAStarAlgorithm.java`: Adapter for external A* algorithm.
  - `src/main/java/com/example/graphapp/adapter/GraphElementView.java`: Interface for adapter pattern.

### Logging
- `src/main/java/com/example/graphapp/strategy/logging/LoggingStrategy.java`: Interface for logging strategies.
- `src/main/java/com/example/graphapp/strategy/logging/ConsoleLoggingStrategy.java`: Logs to console.
- `src/main/java/com/example/graphapp/strategy/logging/FileLoggingStrategy.java`: Logs to file.
- `src/main/java/com/example/graphapp/strategy/logging/DatabaseLoggingStrategy.java`: Logs to database.

### Utilities
- `src/main/java/com/example/graphapp/util/PathResult.java`: Stores path calculation results.

## Configuration
- `pom.xml`: Maven project configuration with dependencies for JavaFX and MySQL.

## Features
- Create and manipulate graph nodes and edges
- Calculate shortest paths using various algorithms
- Different interaction modes (Node, Edge, Path)
- Multiple logging strategies (Console, File, Database)
- Visual representation of graph elements

## Requirements
- Java 11 or higher
- JavaFX 17.0.2
- MySQL (for database logging)
