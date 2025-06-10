package com.example.graphapp.factory;

import com.example.graphapp.strategy.logging.ConsoleLoggingStrategy;
import com.example.graphapp.strategy.logging.DatabaseLoggingStrategy;
import com.example.graphapp.strategy.logging.FileLoggingStrategy;
import com.example.graphapp.strategy.logging.LoggingStrategy;

public class LoggingStrategyFactory {
    public LoggingStrategy createLogger(String type) {
        if (type == null) {
            System.err.println("LoggingStrategyFactory: Logger type is null, defaulting to Console.");
            return new ConsoleLoggingStrategy(); // Comportement par défaut sûr
        }
        switch (type.toUpperCase()) {
            case "CONSOLE":
                return new ConsoleLoggingStrategy();
            case "FILE":
                return new FileLoggingStrategy("application.log"); // <<<< NOM DU FICHIER DE LOG
            case "DATABASE":
                return new DatabaseLoggingStrategy();
            default:
                System.err.println("LoggingStrategyFactory: Unknown logger type '" + type + "', defaulting to Console.");
                // throw new IllegalArgumentException("Unknown logger type: " + type);
                return new ConsoleLoggingStrategy(); // Comportement par défaut sûr
        }
    }
}