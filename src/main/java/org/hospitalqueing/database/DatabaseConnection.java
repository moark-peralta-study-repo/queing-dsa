package org.hospitalqueing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static final String DB_URL = "jdbc:sqlite:hospital.db";

  private static Connection connection = null;

  public static Connection getConnection() {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection(DB_URL);
        System.out.println("Connected to: " + DB_URL);
      } catch (SQLException e) {
        System.err.println("Connection Error: " + e.getMessage());
      }
    }

    return connection;
  }
}
