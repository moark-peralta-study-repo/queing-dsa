package org.hospitalqueing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

  private static final DatabaseConnection SINGLETON = new DatabaseConnection();

  /** The shared instance (used by DAOs, per the shared conventions). */
  public static DatabaseConnection getSingleton() {
    return SINGLETON;
  }

  private static final String DB_URL = "jdbc:sqlite:hospital.db";

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL);
  }

  public static void initializeDatabase() {

    String createRoles =
        """
        CREATE TABLE IF NOT EXISTS roles (
            role_id INTEGER PRIMARY KEY,
            role_name TEXT NOT NULL UNIQUE
        );
        """;

    String createUsers =
        """
        CREATE TABLE IF NOT EXISTS users (
            user_id INTEGER PRIMARY KEY,
            username TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            role_id INTEGER NOT NULL,
            is_active INTEGER NOT NULL DEFAULT 1,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            deleted_at TEXT,

            FOREIGN KEY (role_id)
                REFERENCES roles(role_id)
        );
        """;

    String createDepartments =
        """
        CREATE TABLE IF NOT EXISTS departments (
            department_id INTEGER PRIMARY KEY,
            department_name TEXT NOT NULL UNIQUE,
            is_active INTEGER NOT NULL DEFAULT 1
        );
        """;

    String createPatients =
        """
        CREATE TABLE IF NOT EXISTS patients (
            patient_id INTEGER PRIMARY KEY,
            user_id INTEGER NOT NULL UNIQUE,

            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            middle_name TEXT,
            birth_date TEXT,
            sex TEXT,
            phone TEXT,
            deleted_at TEXT,

            FOREIGN KEY (user_id)
                REFERENCES users(user_id)
        );
        """;

    String createStaff =
        """
        CREATE TABLE IF NOT EXISTS staff (
            staff_id INTEGER PRIMARY KEY,
            user_id INTEGER NOT NULL UNIQUE,

            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            phone TEXT,

            department_id INTEGER,

            FOREIGN KEY (user_id)
                REFERENCES users(user_id),

            FOREIGN KEY (department_id)
                REFERENCES departments(department_id)
        );
        """;

    String createServices =
        """
        CREATE TABLE IF NOT EXISTS services (
            service_id INTEGER PRIMARY KEY,

            department_id INTEGER NOT NULL,
            service_name TEXT NOT NULL,

            avg_service_minutes INTEGER DEFAULT 10,
            is_active INTEGER NOT NULL DEFAULT 1,

            FOREIGN KEY (department_id)
                REFERENCES departments(department_id),

            UNIQUE(department_id, service_name)
        );
        """;

    String createDoctors =
        """
        CREATE TABLE IF NOT EXISTS doctors (
            doctor_id INTEGER PRIMARY KEY,

            user_id INTEGER,

            department_id INTEGER NOT NULL,

            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            license_number TEXT,

            is_active INTEGER NOT NULL DEFAULT 1,

            FOREIGN KEY (user_id)
                REFERENCES users(user_id),

            FOREIGN KEY (department_id)
                REFERENCES departments(department_id)
        );
        """;

    String createDoctorServices =
        """
        CREATE TABLE IF NOT EXISTS doctor_services (
            doctor_id INTEGER NOT NULL,
            service_id INTEGER NOT NULL,

            PRIMARY KEY (doctor_id, service_id),

            FOREIGN KEY (doctor_id)
                REFERENCES doctors(doctor_id),

            FOREIGN KEY (service_id)
                REFERENCES services(service_id)
        );
        """;

    String createCounters =
        """
        CREATE TABLE IF NOT EXISTS counters (
            counter_id INTEGER PRIMARY KEY,

            department_id INTEGER NOT NULL,

            counter_name TEXT NOT NULL,
            room_name TEXT,

            is_active INTEGER NOT NULL DEFAULT 1,

            FOREIGN KEY (department_id)
                REFERENCES departments(department_id)
        );
        """;

    String createAppointments =
        """
        CREATE TABLE IF NOT EXISTS appointments (
            appointment_id INTEGER PRIMARY KEY,

            patient_id INTEGER NOT NULL,
            service_id INTEGER NOT NULL,
            doctor_id INTEGER,

            appointment_date TEXT NOT NULL,
            appointment_time TEXT NOT NULL,

            status TEXT NOT NULL DEFAULT 'SCHEDULED',

            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

            deleted_at TEXT,

            FOREIGN KEY (patient_id)
                REFERENCES patients(patient_id),

            FOREIGN KEY (service_id)
                REFERENCES services(service_id),

            FOREIGN KEY (doctor_id)
                REFERENCES doctors(doctor_id),

            CHECK (
                status IN (
                    'SCHEDULED',
                    'CONFIRMED',
                    'COMPLETED',
                    'CANCELLED',
                    'NO_SHOW'
                )
            )
        );
        """;

    String createQueueEntries =
        """
        CREATE TABLE IF NOT EXISTS queue_entries (
            queue_id INTEGER PRIMARY KEY,

            patient_id INTEGER NOT NULL,
            department_id INTEGER NOT NULL,
            service_id INTEGER NOT NULL,

            doctor_id INTEGER,
            appointment_id INTEGER,
            counter_id INTEGER,

            queue_date TEXT NOT NULL,
            queue_number INTEGER NOT NULL,

            priority_type TEXT NOT NULL DEFAULT 'REGULAR',

            status TEXT NOT NULL DEFAULT 'Waiting',

            qr_token TEXT UNIQUE,

            joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            called_at TEXT,
            service_started_at TEXT,
            completed_at TEXT,
            deleted_at TEXT,

            FOREIGN KEY (patient_id)
                REFERENCES patients(patient_id),

            FOREIGN KEY (department_id)
                REFERENCES departments(department_id),

            FOREIGN KEY (service_id)
                REFERENCES services(service_id),

            FOREIGN KEY (doctor_id)
                REFERENCES doctors(doctor_id),

            FOREIGN KEY (appointment_id)
                REFERENCES appointments(appointment_id),

            FOREIGN KEY (counter_id)
                REFERENCES counters(counter_id),

            CHECK (
                priority_type IN (
                    'REGULAR',
                    'SENIOR',
                    'PWD',
                    'EMERGENCY',
                    'APPOINTMENT'
                )
            ),

            CHECK (
                status IN (
                    'Waiting',
                    'Checked In',
                    'In Consultation',
                    'For Laboratory',
                    'For Pharmacy',
                    'Discharged',
                    'Completed',
                    'No Show'
                )
            ),

            UNIQUE(department_id, queue_date, queue_number)
        );
        """;

    String createQueueEvents =
        """
        CREATE TABLE IF NOT EXISTS queue_events (
            event_id INTEGER PRIMARY KEY,

            queue_id INTEGER NOT NULL,
            staff_id INTEGER,

            event_type TEXT NOT NULL,
            notes TEXT,

            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

            FOREIGN KEY (queue_id)
                REFERENCES queue_entries(queue_id),

            FOREIGN KEY (staff_id)
                REFERENCES staff(staff_id)
        );
        """;

    String createNotifications =
        """
        CREATE TABLE IF NOT EXISTS notifications (
            notification_id INTEGER PRIMARY KEY,

            patient_id INTEGER NOT NULL,
            queue_id INTEGER,

            channel TEXT NOT NULL,
            message TEXT NOT NULL,

            status TEXT NOT NULL DEFAULT 'PENDING',

            sent_at TEXT,

            FOREIGN KEY (patient_id)
                REFERENCES patients(patient_id),

            FOREIGN KEY (queue_id)
                REFERENCES queue_entries(queue_id)
        );
        """;

    String createPayments =
        """
        CREATE TABLE IF NOT EXISTS payments (
            payment_id INTEGER PRIMARY KEY,

            queue_id INTEGER NOT NULL,

            amount REAL NOT NULL DEFAULT 0,
            payment_method TEXT,
            status TEXT NOT NULL DEFAULT 'PENDING',

            paid_at TEXT,

            FOREIGN KEY (queue_id)
                REFERENCES queue_entries(queue_id)
        );
        """;

    String createFeedback =
        """
        CREATE TABLE IF NOT EXISTS feedback (
            feedback_id INTEGER PRIMARY KEY,

            queue_id INTEGER NOT NULL,
            patient_id INTEGER NOT NULL,

            rating INTEGER NOT NULL,
            comment TEXT,

            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

            FOREIGN KEY (queue_id)
                REFERENCES queue_entries(queue_id),

            FOREIGN KEY (patient_id)
                REFERENCES patients(patient_id),

            CHECK (rating BETWEEN 1 AND 5)
        );
        """;

    String createSecurityLogs =
        """
        CREATE TABLE IF NOT EXISTS security_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            action TEXT,
            username TEXT,
            role TEXT,
            success INTEGER,
            host TEXT,
            created_at TEXT
        );
        """;

    String createSystemSettings =
        """
        CREATE TABLE IF NOT EXISTS system_settings (
            key TEXT PRIMARY KEY,
            value TEXT
        );
        """;

    String createBackupHistory =
        """
        CREATE TABLE IF NOT EXISTS backup_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            file_path TEXT,
            file_size INTEGER,
            created_at TEXT
        );
        """;

    String createIncidents =
        """
        CREATE TABLE IF NOT EXISTS incidents (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT,
            description TEXT,
            severity TEXT,
            status TEXT,
            reported_by TEXT,
            created_at TEXT,
            resolved_at TEXT
        );
        """;

    try (Connection connection = getConnection();
        Statement statement = connection.createStatement()) {

      statement.executeUpdate(createRoles);
      statement.executeUpdate(createUsers);
      statement.executeUpdate(createDepartments);
      statement.executeUpdate(createPatients);
      statement.executeUpdate(createStaff);
      statement.executeUpdate(createServices);
      statement.executeUpdate(createDoctors);
      statement.executeUpdate(createDoctorServices);
      statement.executeUpdate(createCounters);
      statement.executeUpdate(createAppointments);
      statement.executeUpdate(createQueueEntries);
      statement.executeUpdate(createQueueEvents);
      statement.executeUpdate(createNotifications);
      statement.executeUpdate(createPayments);
      statement.executeUpdate(createFeedback);
      statement.executeUpdate(createSecurityLogs);
      statement.executeUpdate(createIncidents);
      statement.executeUpdate(createSystemSettings);
      statement.executeUpdate(createBackupHistory);

      // Migrate data from the previous (uppercase) status vocabulary so existing DBs
      // line up with the queue_entries CHECK constraint used by the staff panels.
      // No-op when already on the new vocabulary.
      migrateQueueStatusVocabulary(statement);

      // Doctors gained a login account (DOCTOR role, T1): add the user_id column to
      // existing DBs. No-op when the column already exists.
      migrateDoctorsUserIdColumn(statement);

      // Trash bin (T10): soft-delete support via deleted_at on appointments + queue_entries.
      // No-op when the columns already exist.
      migrateAddDeletedAt(statement);

      // Admin accounts (T12): soft-delete support for users + patients, and a phone
      // column on staff (the accounts edit dialog manages it). No-ops when present.
      migrateAccountsSoftDelete(statement);
      migrateStaffPhone(statement);

      // Security logs (admin security panel): the table is CREATE-IF-NOT-EXISTS above, so this is
      // just a guarded belt-and-braces for very old DBs. No-op when the table already exists.
      migrateSecurityLogs(statement);

      System.out.println("Database initialized successfully.");

    } catch (SQLException e) {
      throw new DatabaseException("Database initialization failed", e);
    }
  }

  /**
   * Rewrites legacy queue status values to the current vocabulary. The mapping is a best effort
   * for the old machine-oriented names:
   *
   * <pre>
   *   WAITING        -> Waiting
   *   CALLED         -> Checked In
   *   IN_SERVICE     -> In Consultation
   *   SKIPPED        -> No Show
   *   TRANSFERRED    -> For Laboratory
   *   CANCELLED      -> No Show
   *   COMPLETED      -> Completed
   *   NO_SHOW        -> No Show
   * </pre>
   *
   * Each update is idempotent (the source value no longer exists afterwards), so this is safe to
   * run on every startup.
   */
  private static void migrateQueueStatusVocabulary(Statement statement) throws SQLException {
    String[][] migrations = {
      {"WAITING", "Waiting"},
      {"CALLED", "Checked In"},
      {"IN_SERVICE", "In Consultation"},
      {"SKIPPED", "No Show"},
      {"TRANSFERRED", "For Laboratory"},
      {"CANCELLED", "No Show"},
      {"COMPLETED", "Completed"},
      {"NO_SHOW", "No Show"},
    };
    for (String[] m : migrations) {
      String sql = "UPDATE queue_entries SET status = '" + m[1] + "' WHERE status = '" + m[0] + "'";
      statement.executeUpdate(sql);
    }
  }

  /**
   * Adds the {@code doctors.user_id} column on existing databases. Doctors only became log-in
   * accounts in T1 (DOCTOR role); pre-existing DBs are missing the column, so we ALTER TABLE
   * when it isn't there yet. Safe to run on every startup (a no-op once the column exists).
   */
  private static void migrateDoctorsUserIdColumn(Statement statement) throws SQLException {
    try (ResultSet rs = statement.executeQuery("PRAGMA table_info(doctors)")) {
      boolean hasUserId = false;
      while (rs.next()) {
        if ("user_id".equals(rs.getString("name"))) {
          hasUserId = true;
        }
      }
      if (!hasUserId) {
        statement.executeUpdate("ALTER TABLE doctors ADD COLUMN user_id INTEGER");
        System.out.println("Migrated doctors table: added user_id column (DOCTOR role support)");
      }
    }
  }

  /**
   * Adds the {@code deleted_at} soft-delete column (T10) to {@code appointments} and
   * {@code queue_entries} on existing databases. Pre-T10 DBs lack the column, so we ALTER TABLE
   * when it isn't there yet. Safe to run on every startup (a no-op once the columns exist).
   */
  private static void migrateAddDeletedAt(Statement statement) throws SQLException {
    addDeletedAtColumn(statement, "appointments");
    addDeletedAtColumn(statement, "queue_entries");
  }

  private static void addDeletedAtColumn(Statement statement, String table) throws SQLException {
    try (ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
      boolean hasDeletedAt = false;
      while (rs.next()) {
        if ("deleted_at".equals(rs.getString("name"))) {
          hasDeletedAt = true;
        }
      }
      if (!hasDeletedAt) {
        statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN deleted_at TEXT");
        System.out.println("Migrated " + table + " table: added deleted_at column (trash bin)");
      }
    }
  }

  /**
   * Extends the trash bin (T12, admin accounts): adds {@code deleted_at} to {@code users} and
   * {@code patients} on existing databases so accounts can be soft-deleted and restored.
   * Safe to run on every startup (no-op once the columns exist).
   */
  private static void migrateAccountsSoftDelete(Statement statement) throws SQLException {
    addDeletedAtColumn(statement, "users");
    addDeletedAtColumn(statement, "patients");
  }

  /**
   * Creates the {@code security_logs} table on existing databases that predate it (the boot
   * schema above is CREATE-IF-NOT-EXISTS, so no ALTER is needed). Safe to run on every startup
   * (no-op once the table exists).
   */
  private static void migrateSecurityLogs(Statement statement) throws SQLException {
    try (ResultSet rs = statement.executeQuery("PRAGMA table_info(security_logs)")) {
      if (rs.next()) {
        return; // table already exists
      }
    }
    statement.executeUpdate(
        """
        CREATE TABLE IF NOT EXISTS security_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            action TEXT,
            username TEXT,
            role TEXT,
            success INTEGER,
            host TEXT,
            created_at TEXT
        );
        """);
    System.out.println("Migrated schema: created security_logs table (admin security logs)");
  }

  /**
   * Adds {@code staff.phone} on existing databases (the accounts edit dialog manages staff
   * contact info). Safe to run on every startup (no-op once the column exists).
   */
  private static void migrateStaffPhone(Statement statement) throws SQLException {
    try (ResultSet rs = statement.executeQuery("PRAGMA table_info(staff)")) {
      boolean hasPhone = false;
      while (rs.next()) {
        if ("phone".equals(rs.getString("name"))) {
          hasPhone = true;
        }
      }
      if (!hasPhone) {
        statement.executeUpdate("ALTER TABLE staff ADD COLUMN phone TEXT");
        System.out.println("Migrated staff table: added phone column (admin accounts)");
      }
    }
  }
}
