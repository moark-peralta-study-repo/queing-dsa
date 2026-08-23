package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.DoctorService;

public class DoctorServiceDAO {

  public void save(DoctorService doctorService) {

    String sql =
        """
          INSERT INTO doctor_services (
            doctor_id,
            service_id
          )
          VALUES (?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, doctorService.getDoctorId());
      statement.setInt(2, doctorService.getServiceId());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<DoctorService> findAll() {

    String sql =
        """
          SELECT *
          FROM doctor_services
        """;

    List<DoctorService> doctorServices = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        doctorServices.add(mapDoctorService(resultSet));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return doctorServices;
  }

  public List<DoctorService> findByDoctor(int doctorId) {

    String sql =
        """
          SELECT *
          FROM doctor_services
          WHERE doctor_id = ?
        """;

    List<DoctorService> doctorServices = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, doctorId);

      try (ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
          doctorServices.add(mapDoctorService(resultSet));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return doctorServices;
  }

  public List<DoctorService> findByService(int serviceId) {

    String sql =
        """
          SELECT *
          FROM doctor_services
          WHERE service_id = ?
        """;

    List<DoctorService> doctorServices = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, serviceId);

      try (ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
          doctorServices.add(mapDoctorService(resultSet));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return doctorServices;
  }

  public void delete(int doctorId, int serviceId) {

    String sql =
        """
          DELETE FROM doctor_services
          WHERE doctor_id = ?
            AND service_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, doctorId);
      statement.setInt(2, serviceId);

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private DoctorService mapDoctorService(ResultSet resultSet) throws SQLException {

    DoctorService doctorService =
        new DoctorService(
            resultSet.getInt("doctor_id"),
            resultSet.getInt("service_id"));

    return doctorService;
  }
}
