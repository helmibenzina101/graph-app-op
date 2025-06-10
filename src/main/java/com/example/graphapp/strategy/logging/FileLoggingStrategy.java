package com.example.graphapp.strategy.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths; // Pour obtenir le chemin de travail courant

public class FileLoggingStrategy implements LoggingStrategy {
    private final String logFilePath;

    // Constructeur utilisé par la factory
    public FileLoggingStrategy(String fileName) {
        // Place le fichier de log dans le répertoire de travail courant de l'application
        this.logFilePath = Paths.get("").toAbsolutePath().resolve(fileName).toString();
        System.out.println("FileLoggingStrategy: Logs will be written to: " + this.logFilePath);
    }

    @Override
    public void log(String message) {
        try (FileWriter fw = new FileWriter(logFilePath, true); // true pour ajouter au fichier existant
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);
        } catch (IOException e) {
            System.err.println("Error writing to log file (" + logFilePath + "): " + e.getMessage());
            // Fallback vers la console si l'écriture dans le fichier échoue
            new ConsoleLoggingStrategy().log("FILE LOG FAILED: " + message);
        }
    }
}