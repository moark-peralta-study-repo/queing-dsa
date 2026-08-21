package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.QueueEntry;

public class QueueEntryDAO {

  public void save(QueueEntry queueEntry) {
    String sql =
        """
          INSERT INTO queue_entries (
            queue_id,
            patient_id,
            department_id,
            service_id,
            doctor_id,
            appointment_id,
            counter_id,
            queue_date,
            queue_number,
            priority_type,
            status,
            qr_token,
            joined_at,
            called_at,
            service_started_at,
            completed_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, queueEntry.getQueueId());
      statement.setInt(2, queueEntry.getPatientId());
      statement.setInt(3, queueEntry.getDepartmentId());
      statement.setInt(4, queueEntry.getServiceId());

      if (queueEntry.getDoctorId() != null) {
        statement.setInt(5, queueEntry.getDoctorId());
      } else {
        statement.setNull(5, java.sql.Types.INTEGER);
      }

      if (queueEntry.getAppointmentId() != null) {
        statement.setInt(6, queueEntry.getAppointmentId());
      } else {
        statement.setNull(6, java.sql.Types.INTEGER);
      }

      if (queueEntry.getCounterId() != null) {
        statement.setInt(7, queueEntry.getCounterId());
      } else {
        statement.setNull(7, java.sql.Types.INTEGER);
      }

      statement.setString(8, queueEntry.getQueueDate());
      statement.setInt(9, queueEntry.getQueueNumber());
      statement.setString(10, queueEntry.getPriorityType());
      statement.setString(11, queueEntry.getStatus());
      statement.setString(12, queueEntry.getQrToken());
      statement.setString(13, queueEntry.getJoinedAt());
      statement.setString(14, queueEntry.getCalledAt());
      statement.setString(15, queueEntry.getServiceStartedAt());
      statement.setString(16, queueEntry.getCompletedAt());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public QueueEntry findById(int queueId) {

    String sql =
        """
        SELECT *
        FROM queue_entries
        WHERE queue_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, queueId);

      try (ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
          return mapQueueEntry(resultSet);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<QueueEntry> findAll() {

    String sql =
        """
          SELECT *
          FROM queue_entries
        """;

    List<QueueEntry> queueEntries = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        queueEntries.add(mapQueueEntry(resultSet));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return queueEntries;
  }

  public void update(QueueEntry queueEntry) {

    String sql =
        """
          UPDATE queue_entries
          SET patient_id = ?,
          department_id = ?,
          service_id = ?,
          doctor_id = ?,
          appointment_id = ?,
          counter_id = ?,
          queue_date = ?,
          queue_number = ?,
          priority_type = ?,
          status = ?,
          qr_token = ?,
          joined_at = ?,
          called_at = ?,
          service_started_at = ?,
          completed_at = ?
          WHERE queue_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, queueEntry.getPatientId());
      statement.setInt(2, queueEntry.getDepartmentId());
      statement.setInt(3, queueEntry.getServiceId());

      if (queueEntry.getDoctorId() != null) {
        statement.setInt(4, queueEntry.getDoctorId());
      } else {
        statement.setNull(4, java.sql.Types.INTEGER);
      }

      if (queueEntry.getAppointmentId() != null) {
        statement.setInt(5, queueEntry.getAppointmentId());
      } else {
        statement.setNull(5, java.sql.Types.INTEGER);
      }

      if (queueEntry.getCounterId() != null) {
        statement.setInt(6, queueEntry.getCounterId());
      } else {
        statement.setNull(6, java.sql.Types.INTEGER);
      }

      statement.setString(7, queueEntry.getQueueDate());
      statement.setInt(8, queueEntry.getQueueNumber());
      statement.setString(9, queueEntry.getPriorityType());
      statement.setString(10, queueEntry.getStatus());
      statement.setString(11, queueEntry.getQrToken());
      statement.setString(12, queueEntry.getJoinedAt());
      statement.setString(13, queueEntry.getCalledAt());
      statement.setString(14, queueEntry.getServiceStartedAt());
      statement.setString(15, queueEntry.getCompletedAt());
      statement.setInt(16, queueEntry.getQueueId());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void delete(int queueId) {

    String sql =
        """
          DELETE FROM queue_entries
          WHERE queue_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, queueId);

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private QueueEntry mapQueueEntry(ResultSet resultSet) throws SQLException {

    QueueEntry queueEntry = new QueueEntry();

    queueEntry.setQueueId(resultSet.getInt("queue_id"));
    queueEntry.setPatientId(resultSet.getInt("patient_id"));
    queueEntry.setDepartmentId(resultSet.getInt("department_id"));
    queueEntry.setServiceId(resultSet.getInt("service_id"));

    queueEntry.setDoctorId((Integer) resultSet.getObject("doctor_id"));

    queueEntry.setAppointmentId((Integer) resultSet.getObject("appointment_id"));

    queueEntry.setCounterId((Integer) resultSet.getObject("counter_id"));

    queueEntry.setQueueDate(resultSet.getString("queue_date"));

    queueEntry.setQueueNumber(resultSet.getInt("queue_number"));

    queueEntry.setPriorityType(resultSet.getString("priority_type"));

    queueEntry.setStatus(resultSet.getString("status"));

    queueEntry.setQrToken(resultSet.getString("qr_token"));

    queueEntry.setJoinedAt(resultSet.getString("joined_at"));

    queueEntry.setCalledAt(resultSet.getString("called_at"));

    queueEntry.setServiceStartedAt(resultSet.getString("service_started_at"));

    queueEntry.setCompletedAt(resultSet.getString("completed_at"));

    return queueEntry;
  }
}
