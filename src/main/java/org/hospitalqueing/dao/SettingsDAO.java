package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.database.DatabaseException;

/**
 * Key/value access to the {@code system_settings} table (system settings storage).
 *
 * <p>Values are plain strings; callers decide how to interpret them (numbers, booleans,
 * timestamps, ...). Missing keys return {@code null} rather than throwing.
 */
public class SettingsDAO {

  /**
   * Reads the value stored for {@code key}.
   *
   * @return the stored value, or {@code null} when the key does not exist (or is NULL).
   */
  public String get(String key) {

    String sql =
        """
          SELECT value
          FROM system_settings
          WHERE key = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, key);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString("value");
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return null;
  }

  /**
   * Inserts or overwrites the value for {@code key} (upsert).
   */
  public void put(String key, String value) {

    String sql =
        """
          INSERT INTO system_settings (key, value)
          VALUES (?, ?)
          ON CONFLICT(key) DO UPDATE SET value = excluded.value
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, key);
      statement.setString(2, value);

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /**
   * Deletes the row for {@code key} (no-op when the key does not exist).
   */
  public void delete(String key) {

    String sql =
        """
          DELETE FROM system_settings
          WHERE key = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, key);

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }
}
