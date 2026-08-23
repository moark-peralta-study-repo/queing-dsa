package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.Department;

public class DepartmentDAO {

  public void save(Department department) {
    String sql =
        """
              INSERT INTO departments (
              department_name,
              is_active
            )
            VALUES (?, ?)
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, department.getDepartmentName());
      stmt.setInt(2, department.getIsActive() ? 1 : 0);

      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Department findById(int departmentId) {
    String sql =
        """
         SELECT * FROM departments
          WHERE department_id = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, departmentId);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapDepartment(rs);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<Department> findAll() {
    String sql =
        """
            SELECT * FROM departments
        """;

    List<Department> departments = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        departments.add(mapDepartment(rs));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return departments;
  }

  public void delete(int departmentId) {
    String sql =
        """
          DELETE FROM departments
          WHERE department_id = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, departmentId);

      stmt.executeUpdate();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void update(Department department) {

    String sql =
        """
            UPDATE departments
            SET department_name = ?,
                is_active = ?
        WHERE department_id = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, department.getDepartmentName());
      stmt.setInt(2, department.getIsActive() ? 1 : 0);
      stmt.setInt(3, department.getDepartmentId());

      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Department mapDepartment(ResultSet rs) throws SQLException {
    Department department = new Department();

    department.setDepartmentId(rs.getInt("department_id"));
    department.setDepartmentName(rs.getString("department_name"));
    department.setIsActive(rs.getInt("is_active") == 1);

    return department;
  }
}
