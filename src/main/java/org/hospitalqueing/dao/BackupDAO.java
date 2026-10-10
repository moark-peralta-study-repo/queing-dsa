package org.hospitalqueing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.database.DatabaseException;

/**
 * Access to the {@code backup_history} table: every database backup the system has taken.
 */
public class BackupDAO {

  /** One backup_history row, as returned by {@link #list()}. */
  public record BackupEntry(String filePath, long fileSize, String createdAt) {}

  private static final DateTimeFormatter ISO_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Records a taken backup: the file path, its size, and the current time as an ISO timestamp
   * (UTC wall clock of the app host, second precision).
   */
  public void record(String filePath, long fileSize) {

    String sql =
        """
          INSERT INTO backup_history (file_path, file_size, created_at)
          VALUES (?, ?, ?)
        """;

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, filePath);
      statement.setLong(2, fileSize);
      statement.setString(3, LocalDateTime.now().format(ISO_FMT));

      statement.executeUpdate();

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }
  }

  /**
   * All recorded backups, newest first.
   */
  public List<BackupEntry> list() {

    String sql =
        """
          SELECT file_path, file_size, created_at
          FROM backup_history
          ORDER BY created_at DESC, id DESC
        """;

    List<BackupEntry> entries = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        entries.add(
            new BackupEntry(
                resultSet.getString("file_path"),
                resultSet.getLong("file_size"),
                resultSet.getString("created_at")));
      }

    } catch (SQLException e) {
      throw new DatabaseException("Database operation failed", e);
    }

    return entries;
  }

  /**
   * The most recent recorded backup, or {@code null} when no backup has been taken yet.
   */
  public BackupEntry latest() {
    List<BackupEntry> entries = list();
    return entries.isEmpty() ? null : entries.get(0);
  }
}
