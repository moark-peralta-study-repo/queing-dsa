package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.Service;

public class ServiceDAO {

  public void save(Service service) {

    String sql =
        """
          INSERT INTO services (
            department_id,
            service_name,
            avg_service_minutes,
            is_active
          )
          VALUES (?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, service.getDepartmentId());
      statement.setString(2, service.getServiceName());
      statement.setInt(3, service.getAvgServiceMinutes());
      statement.setInt(4, service.isActive() ? 1 : 0);

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Service findById(int serviceId) {

    String sql =
        """
          SELECT *
          FROM services
          WHERE service_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, serviceId);

      try (ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
          return mapService(resultSet);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<Service> findAll() {

    String sql =
        """
          SELECT *
          FROM services
        """;

    List<Service> services = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        services.add(mapService(resultSet));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return services;
  }

  public void update(Service service) {

    String sql =
        """
          UPDATE services
          SET department_id = ?,
              service_name = ?,
              avg_service_minutes = ?,
              is_active = ?
          WHERE service_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, service.getDepartmentId());
      statement.setString(2, service.getServiceName());
      statement.setInt(3, service.getAvgServiceMinutes());
      statement.setInt(4, service.isActive() ? 1 : 0);
      statement.setInt(5, service.getServiceId());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void delete(int serviceId) {

    String sql =
        """
          DELETE FROM services
          WHERE service_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, serviceId);

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Service mapService(ResultSet resultSet) throws SQLException {

    Service service = new Service();

    service.setServiceId(resultSet.getInt("service_id"));
    service.setDepartmentId(resultSet.getInt("department_id"));
    service.setServiceName(resultSet.getString("service_name"));
    service.setAvgServiceMinutes(resultSet.getInt("avg_service_minutes"));
    service.setActive(resultSet.getInt("is_active") == 1);

    return service;
  }
}
