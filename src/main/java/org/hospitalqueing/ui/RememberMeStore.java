package org.hospitalqueing.ui;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Tiny persistence for the login "Remember me" feature (T7).
 *
 * <p>Stores <b>only the username</b> — never the password and never its hash — in a small
 * {@code .properties} file in the working directory, alongside the SQLite database
 * ({@code hospital.db}). The file location matches the app's cwd-relative storage convention.
 *
 * <p>A missing, empty, or corrupt file is treated as "no remembered username" and never throws:
 * {@link #readUsername()} swallows any I/O or parse problem and returns {@code null}, and
 * {@link #writeUsername(String)} swallows write problems so a persistence hiccup can never break
 * the login itself.
 */
public final class RememberMeStore {

  /** Name of the remember-me file, relative to the working directory. */
  public static final String FILE_NAME = ".login_rememberme.properties";

  /** The single key used to persist the username. */
  public static final String USERNAME_KEY = "username";

  private RememberMeStore() {}

  private static Path file() {
    return Paths.get(FILE_NAME);
  }

  /**
   * Returns the previously remembered username, or {@code null} when there is none (file missing,
   * empty, has no {@value #USERNAME_KEY} entry, or is corrupt/unreadable).
   */
  public static String readUsername() {
    try {
      if (!Files.exists(file())) {
        return null;
      }
      Properties props = new Properties();
      try (InputStream in = Files.newInputStream(file())) {
        props.load(in);
      }
      String username = props.getProperty(USERNAME_KEY);
      if (username == null) {
        return null;
      }
      username = username.trim();
      return username.isEmpty() ? null : username;
    } catch (Exception e) {
      // Missing/corrupt file is a "do not prefill" condition, not an error.
      return null;
    }
  }

  /**
   * Remembers the given username. Pass {@code null} or a blank username to forget, which deletes
   * the file. Best-effort: any write failure is swallowed so it cannot break the login flow.
   */
  public static void writeUsername(String username) {
    try {
      if (username == null || username.trim().isEmpty()) {
        Files.deleteIfExists(file());
        return;
      }
      Properties props = new Properties();
      props.setProperty(USERNAME_KEY, username.trim());
      try (OutputStream out = Files.newOutputStream(file(),
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE)) {
        props.store(out, "Remember-me login: username only (never the password or its hash)");
      }
    } catch (Exception e) {
      // Persistence is best-effort; a failure must never surface to the login UI.
    }
  }
}
