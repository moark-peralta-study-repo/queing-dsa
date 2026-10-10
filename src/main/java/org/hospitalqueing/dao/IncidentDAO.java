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
import org.hospitalqueing.model.Incident;

/**
 * Data access for the {@code incidents} table (admin INCIDENT REPORTS screen). Severity and
 * status are stored as their enum names; timestamps are ISO-8601 local strings, matching the
 * rest of the schema.
 */
public class IncidentDAO {

  /**
   * Inserts a new incident (status defaults to OPEN when unset). The generated {@code id} is
   * written back onto the passed model, following the other DAOs' save pattern.
   */
  public void insert(Incident incident) {
    if (incident.getStatus() == null) {
      incident.setStatus(Incident.Status.OPEN);
    }
    String sql =
        """
          INSERT INTO incidents (
            title,
            description,
            severity,
            status,
            reported_by,
            created_at,
            resolved_at
          )
          VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, incident.getTitle());
      statement.setString(2, incident.getDescription());
      statement.setString(3, incident.getSeverity() == null ? null : incident.getSeverity().name());
      statement.setString(4, incident.getStatus().name());
      statement.setString(5, incident.getReportedBy());
      statement.setString(6, incident.getCreatedAt() == null ? null : incident.getCreatedAt().withNano(0).toString());
      statement.setString(7, incident.getResolvedAt() == null ? null : incident.getResolvedAt().withNano(0).toString());

      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          incident.setId(keys.getInt(1));
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /**
   * Moves an incident to a new status. {@code resolvedAt} is stamped by the caller when the new
   * status is RESOLVED (pass null to leave the column untouched — it only ever goes in, not out).
   */
  public void updateStatus(int id, Incident.Status status, java.time.LocalDateTime resolvedAt) {
    String sql =
        resolvedAt == null
            ? "UPDATE incidents SET status = ? WHERE id = ?"
            : "UPDATE incidents SET status = ?, resolved_at = ? WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, status.name());
      if (resolvedAt != null) {
        statement.setString(2, resolvedAt.withNano(0).toString());
        statement.setInt(3, id);
      } else {
        statement.setInt(2, id);
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  public Incident findById(int id) {
    String sql = "SELECT * FROM incidents WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return mapIncident(resultSet);
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return null;
  }

  /** All incidents, newest first. */
  public List<Incident> findAll() {
    String sql = "SELECT * FROM incidents ORDER BY created_at DESC, id DESC";

    List<Incident> incidents = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        incidents.add(mapIncident(resultSet));
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return incidents;
  }

  /** All incidents currently in the given status, newest first. */
  public List<Incident> findByStatus(Incident.Status status) {
    String sql =
        """
          SELECT * FROM incidents
          WHERE status = ?
          ORDER BY created_at DESC, id DESC
        """;

    List<Incident> incidents = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, status.name());

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          incidents.add(mapIncident(resultSet));
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return incidents;
  }

  private Incident mapIncident(ResultSet resultSet) throws SQLException {
    Incident incident = new Incident();

    incident.setId(resultSet.getInt("id"));
    incident.setTitle(resultSet.getString("title"));
    incident.setDescription(resultSet.getString("description"));
    incident.setSeverity(parseEnum(resultSet.getString("severity"), Incident.Severity.class, Incident.Severity.LOW));
    incident.setStatus(parseEnum(resultSet.getString("status"), Incident.Status.class, Incident.Status.OPEN));
    incident.setReportedBy(resultSet.getString("reported_by"));
    incident.setCreatedAt(parseDateTime(resultSet.getString("created_at")));
    incident.setResolvedAt(parseDateTime(resultSet.getString("resolved_at")));

    return incident;
  }

  private static <E extends Enum<E>> E parseEnum(String value, Class<E> type, E fallback) {
    if (value == null) {
      return fallback;
    }
    try {
      return Enum.valueOf(type, value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return fallback;
    }
  }

  private static java.time.LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return java.time.LocalDateTime.parse(value.replace(' ', 'T'));
    } catch (java.time.format.DateTimeParseException e) {
      return null;
    }
  }
}
