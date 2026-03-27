package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // CORRECTED DB_NAME: mentor_connect_app
    private static final String DB_NAME = "mentor_connect_app";

    // The URL uses the correct DB_NAME
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASSWORD = "Shin@maya";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found! Ensure the JAR file is included in your project.");
            throw new SQLException("JDBC Driver not available.", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Check if MySQL server is running and credentials are correct.");
            throw e;
        }
    }
}