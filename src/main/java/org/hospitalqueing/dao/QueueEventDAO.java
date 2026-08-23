package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.QueueEvent;

public class QueueEventDAO {

  public void save(QueueEvent queueEvent) {

    String sql =
        """
          INSERT INTO queue_events (
            queue_id,
            staff_id,
            event_type,
            notes
          )
          VALUES (?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, queueEvent.getQueueId());

      if (queueEvent.getStaffId() != null) {
        statement.setInt(2, queueEvent.getStaffId());
      } else {
        statement.setNull(2, java.sql.Types.INTEGER);
      }

      statement.setString(3, queueEvent.getEventType());
      statement.setString(4, queueEvent.getNotes());

      statement.executeUpdate();

      // SQLite generates event_id
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          queueEvent.setEventId(keys.getInt(1));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public QueueEvent findById(int eventId) {

    String sql =
        """
          SELECT *
          FROM queue_events
          WHERE event_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, eventId);

      try (ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
          return mapQueueEvent(resultSet);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<QueueEvent> findAll() {

    String sql =
        """
          SELECT *
          FROM queue_events
        """;

    List<QueueEvent> queueEvents = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        queueEvents.add(mapQueueEvent(resultSet));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return queueEvents;
  }

  public void update(QueueEvent queueEvent) {

    String sql =
        """
          UPDATE queue_events
          SET queue_id = ?,
              staff_id = ?,
              event_type = ?,
              notes = ?
          WHERE event_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, queueEvent.getQueueId());

      if (queueEvent.getStaffId() != null) {
        statement.setInt(2, queueEvent.getStaffId());
      } else {
        statement.setNull(2, java.sql.Types.INTEGER);
      }

      statement.setString(3, queueEvent.getEventType());
      statement.setString(4, queueEvent.getNotes());

      statement.setInt(5, queueEvent.getEventId());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void delete(int eventId) {

    String sql =
        """
          DELETE FROM queue_events
          WHERE event_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, eventId);

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private QueueEvent mapQueueEvent(ResultSet resultSet) throws SQLException {

    QueueEvent queueEvent = new QueueEvent();

    queueEvent.setEventId(resultSet.getInt("event_id"));

    queueEvent.setQueueId(resultSet.getInt("queue_id"));

    queueEvent.setStaffId((Integer) resultSet.getObject("staff_id"));

    queueEvent.setEventType(resultSet.getString("event_type"));

    queueEvent.setNotes(resultSet.getString("notes"));

    String createdAt = resultSet.getString("created_at");

    if (createdAt != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      queueEvent.setCreatedAt(LocalDateTime.parse(createdAt, formatter));
    }

    return queueEvent;
  }
}
