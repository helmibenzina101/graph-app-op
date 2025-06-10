package com.example.graphapp.strategy.logging;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseLoggingStrategy implements LoggingStrategy {
    // --- Configuration MySQL pour XAMPP (root sans mot de passe) ---
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306"; // Port MySQL par défaut
    // IMPORTANT : Remplacez "graph_logs_db" par le nom réel de la base de données
    // que vous avez créée dans XAMPP (phpMyAdmin) pour stocker les logs.
    // Si vous n'en avez pas créé une spécifique, vous pourriez utiliser "test" pour commencer,
    // mais il est préférable de créer une base dédiée.
    private static final String DB_NAME = "graph_logs_db"; // << ASSUREZ-VOUS QUE CETTE BASE EXISTE
    private static final String DB_USER = "root";       // << UTILISATEUR XAMPP PAR DÉFAUT
    private static final String DB_PASSWORD = "";         // << MOT DE PASSE VIDE POUR XAMPP root PAR DÉFAUT

    // Construction de l'URL JDBC
    // Le nom de la base de données (DB_NAME) doit être dans l'URL.
    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
                                         "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useLegacyDatetimeCode=false";

    private Connection connection;

    public DatabaseLoggingStrategy() {
        try {
            // Le chargement explicite du driver n'est généralement plus nécessaire avec JDBC 4.0+
            // Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Attempting to connect to MySQL with URL: " + JDBC_URL + " , User: " + DB_USER);
            this.connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();
            System.out.println("DatabaseLoggingStrategy connected to MySQL database: " + DB_NAME);
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database (" + JDBC_URL + ") as user '" + DB_USER + "'. Error: " + e.getMessage());
            e.printStackTrace();
            this.connection = null; // Assurer que la connexion est nulle si échec
        }
        // catch (ClassNotFoundException e) {
        //     System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        //     e.printStackTrace();
        //     this.connection = null;
        // }
    }

    private void initializeDatabase() throws SQLException {
        if (connection == null) {
            System.err.println("Cannot initialize database, connection is null.");
            return;
        }

        // Création de la table 'logs' si elle n'existe pas
        String createTableSQL = "CREATE TABLE IF NOT EXISTS logs (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + // Mettre une valeur par défaut
                                "message VARCHAR(1024) NOT NULL" + // Message ne devrait pas être null
                                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'logs' checked/created in MySQL database '" + DB_NAME + "'.");
        }
    }

    @Override
    public void log(String message) {
        if (connection == null) {
            System.err.println("MySQL Database connection not available. Logging to console instead.");
            new ConsoleLoggingStrategy().log("DB LOG FAILED (MySQL - No Connection): " + message);
            return;
        }

        String insertSQL = "INSERT INTO logs (timestamp, message) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging to MySQL database: " + e.getMessage());
            // Fallback vers la console si l'écriture dans le fichier échoue
            new ConsoleLoggingStrategy().log("DB LOG FAILED (MySQL - Insert Error): " + message);
        }
    }

    // Méthode pour fermer la connexion, à appeler lors de l'arrêt de l'application
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("MySQL Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing MySQL database connection: " + e.getMessage());
        }
    }
}