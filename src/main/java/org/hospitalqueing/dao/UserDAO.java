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
      throw new DatabaseException("Database operation failed", e);
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
      throw new DatabaseException("Database operation failed", e);
    }

    return null;
  }

  /** Returns the user with the given (case-sensitive) username, or {@code null} if none. */
  public User findByUsername(String username) {
    String sql =
        """
            SELECT *
            FROM users
            WHERE username = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, username);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return mapUser(resultSet);
        }
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
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
      throw new DatabaseException("Database operation failed", e);
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
      throw new DatabaseException("Database operation failed", e);
    }
  }

  public List<User> findAll() {
    // Live accounts only; soft-deleted (trashed) accounts are listed by findTrashed().
    String sql =
        """
        SELECT * FROM users
        WHERE deleted_at IS NULL
        ORDER BY user_id
        """;

    List<User> users = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        users.add(mapUser(resultSet));
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return users;
  }

  public User mapUser(ResultSet resultSet) throws SQLException {
    User user = new User();

    user.setUserId(resultSet.getInt("user_id"));
    user.setUsername(resultSet.getString("username"));
    user.setPasswordHash(resultSet.getString("password_hash"));
    user.setRoleId(resultSet.getInt("role_id"));
    user.setActive(resultSet.getInt("is_active") == 1);

    String createdAt = resultSet.getString("created_at");
    if (createdAt != null) {
      user.setCreatedAt(java.time.LocalDateTime.parse(createdAt.replace(" ", "T")));
    }

    user.setDeletedAt(resultSet.getString("deleted_at"));

    return user;
  }

  /** All soft-deleted (trashed) accounts, most recently deleted first. */
  public List<User> findTrashed() {
    String sql =
        """
        SELECT * FROM users
        WHERE deleted_at IS NOT NULL
        ORDER BY deleted_at DESC
        """;

    List<User> users = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        users.add(mapUser(resultSet));
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return users;
  }

  /** Soft-deletes: stamps deleted_at so the account moves to the trash bin (it stays recoverable). */
  public void softDelete(int userId) {
    String sql = """
        UPDATE users SET deleted_at = ? WHERE user_id = ? AND deleted_at IS NULL
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, java.time.LocalDateTime.now().withNano(0).toString());
      statement.setInt(2, userId);

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /** Restores a soft-deleted account back to live (clears deleted_at). */
  public void restore(int userId) {
    String sql = """
        UPDATE users SET deleted_at = NULL WHERE user_id = ?
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, userId);

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /** Hard delete (from the trash bin): removes the account row for good. */
  public void permanentlyDelete(int userId) {
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
      throw new DatabaseException("Database operation failed", e);
    }
  }
}
