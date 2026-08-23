package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.User;

public class UserDAO {
  public void save(User user) {
    String sql =
        """
          INSERT INTO users (
            username,
            password_hash,
            role_id,
            is_active
        )
        VALUES(?, ?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, user.getUsername());
      statement.setString(2, user.getPasswordHash());
      statement.setInt(3, user.getRoleId());
      statement.setInt(4, user.isActive() ? 1 : 0);

      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          user.setUserId(keys.getInt(1));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public User findById(int userId) {
    String sql =
        """
            SELECT *
            FROM users
            WHERE user_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, userId);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return mapUser(resultSet);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void delete(int userId) {
    String sql =
        """
            DELETE FROM users
            WHERE user_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, userId);

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void update(User user) {
    String sql =
        """
          UPDATE users
          SET username = ?,
              password_hash = ?,
              role_id = ?,
              is_active = ?
          WHERE user_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, user.getUsername());
      statement.setString(2, user.getPasswordHash());
      statement.setInt(3, user.getRoleId());
      statement.setInt(4, user.isActive() ? 1 : 0);
      statement.setInt(5, user.getUserId());

      statement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<User> findAll() {
    String sql =
        """
        SELECT * FROM users
        """;

    List<User> users = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        users.add(mapUser(resultSet));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return users;
  }

  public User mapUser(ResultSet resultSet) throws SQLException {
    User user = new User();

    user.setUsername(resultSet.getString("username"));
    user.setPasswordHash(resultSet.getString("password_hash"));
    user.setRoleId(resultSet.getInt("role_id"));
    user.setActive(resultSet.getInt("is_active") == 1);

    return user;
  }
}
