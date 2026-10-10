package org.hospitalqueing.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.hospitalqueing.dao.BackupDAO;

/**
 * Creates and restores database backups of the live SQLite file ({@code hospital.db}).
 *
 * <p>Backups are timestamped copies of the DB file stored under {@code backups/} in the working
 * directory, and each taken backup is recorded in {@code backup_history} via {@link BackupDAO}.
 *
 * <p>Safe while the app is running: every DAO call opens a fresh JDBC handle, so copying the file
 * over takes a consistent snapshot and the running connection is unaffected. After a restore the
 * UI tells the user to restart the app so any in-memory caches reload.
 */
public class BackupService {

  /** Live database file (relative to the working dir; matches {@code DatabaseConnection.DB_URL}). */
  public static final String DB_FILE = "hospital.db";

  /** Where backups are written. */
  public static final Path BACKUPS_DIR = Paths.get("backups");

  private static final DateTimeFormatter BACKUP_NAME_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

  private final BackupDAO backupDAO = new BackupDAO();

  /**
   * Backs up the live database: copies {@code hospital.db} to
   * {@code backups/backup-<yyyyMMdd-HHmmss>.db}, records it in {@code backup_history}, and returns
   * the new file's path.
   *
   * @throws RuntimeException on any I/O or DB failure
   */
  public String createBackup() {
    try {
      Path source = Paths.get(DB_FILE);
      if (!Files.exists(source)) {
        throw new IllegalStateException("Database file not found: " + source.toAbsolutePath());
      }

      Files.createDirectories(BACKUPS_DIR);

      String stamp = LocalDateTime.now().format(BACKUP_NAME_FMT);
      Path dest = BACKUPS_DIR.resolve("backup-" + stamp + ".db");

      // Two backups in the same second would collide; nudge the name if so.
      while (Files.exists(dest)) {
        stamp = stamp + "-x";
        dest = BACKUPS_DIR.resolve("backup-" + stamp + ".db");
      }

      Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);

      long size = Files.size(dest);
      backupDAO.record(dest.toString(), size);

      return dest.toString();
    } catch (IOException e) {
      throw new RuntimeException("Could not create database backup: " + e.getMessage(), e);
    }
  }

  /**
   * Restores the newest recorded backup over {@code hospital.db}.
   *
   * @return the path of the backup file that was restored
   * @throws IllegalStateException when no backup has been taken yet or the newest file is missing
   */
  public String restoreLatest() {
    var latest = backupDAO.latest();
    if (latest == null) {
      throw new IllegalStateException("No backup found to restore");
    }

    Path source = Paths.get(latest.filePath());
    if (!Files.exists(source)) {
      throw new IllegalStateException("Backup file not found: " + source.toAbsolutePath());
    }

    try {
      Path dest = Paths.get(DB_FILE);
      Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
      return source.toString();
    } catch (IOException e) {
      throw new RuntimeException("Could not restore database backup: " + e.getMessage(), e);
    }
  }

  /** Human-readable size for the panel ("1.2 MB"). */
  public static String humanSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    double size = bytes;
    String[] units = {"KB", "MB", "GB", "TB"};
    int i = -1;
    do {
      size /= 1024;
      i++;
    } while (size >= 1024 && i < units.length - 1);
    return String.format("%.1f %s", size, units[i]);
  }
}
