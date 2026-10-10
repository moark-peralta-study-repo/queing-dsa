package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.database.DatabaseException;
import org.hospitalqueing.model.SecurityLog;

/**
 * security_logs DAO: append-only auth-event log. Reads filter by action and/or username and
 * always come back newest-first (created_at DESC, id DESC as a tie-breaker within the same
 * second).
 */
public class SecurityLogDAO {

  private final DatabaseConnection db;

  public SecurityLogDAO(DatabaseConnection db) {
    this.db = db;
  }

  public void insert(SecurityLog securityLog) {

    String sql =
        """
          INSERT INTO security_logs (
            action,
            username,
            role,
            success,
            host,
            created_at
          )
          VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, securityLog.getAction());
      statement.setString(2, securityLog.getUsername());
      statement.setString(3, securityLog.getRole() == null ? "" : securityLog.getRole());
      statement.setInt(4, securityLog.isSuccess() ? 1 : 0);
      statement.setString(5, securityLog.getHost() == null ? "" : securityLog.getHost());
      statement.setString(6, securityLog.getCreatedAt());

      statement.executeUpdate();

      // SQLite generates the id (AUTOINCREMENT)
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          securityLog.setId(keys.getInt(1));
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /**
   * Events newest-first, optionally filtered by action (null = all) and/or username (null or
   * empty = all; case-insensitive LIKE).
   */
  public List<SecurityLog> findByFilter(String actionOrNull, String usernameOrNull) {

    StringBuilder sql = new StringBuilder(
        """
          SELECT *
          FROM security_logs
          WHERE 1 = 1
        """);
    List<Object> params = new ArrayList<>();

    if (actionOrNull != null && !actionOrNull.isEmpty()) {
      sql.append(" AND action = ?");
      params.add(actionOrNull);
    }
    if (usernameOrNull != null && !usernameOrNull.isEmpty()) {
      sql.append(" AND username LIKE ?");
      params.add(usernameOrNull);
    }
    sql.append(" ORDER BY created_at DESC, id DESC");

    List<SecurityLog> securityLogs = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString())) {

      for (int i = 0; i < params.size(); i++) {
        statement.setObject(i + 1, params.get(i));
      }

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          securityLogs.add(mapSecurityLog(resultSet));
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return securityLogs;
  }

  /** Total number of recorded events (all filters off). */
  public long count() {

    String sql = """
        SELECT COUNT(*)
        FROM security_logs
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      if (resultSet.next()) {
        return resultSet.getLong(1);
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return 0L;
  }

  private SecurityLog mapSecurityLog(ResultSet resultSet) throws SQLException {

    SecurityLog securityLog = new SecurityLog();

    securityLog.setId(resultSet.getInt("id"));
    securityLog.setAction(resultSet.getString("action"));
    securityLog.setUsername(resultSet.getString("username"));
    securityLog.setRole(resultSet.getString("role"));
    securityLog.setSuccess(resultSet.getInt("success") == 1);
    securityLog.setHost(resultSet.getString("host"));
    securityLog.setCreatedAt(resultSet.getString("created_at"));

    return securityLog;
  }
}
