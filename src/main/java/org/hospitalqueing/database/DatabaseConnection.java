package org.hospitalqueing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

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

            department_id INTEGER NOT NULL,

            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            license_number TEXT,

            is_active INTEGER NOT NULL DEFAULT 1,

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

            status TEXT NOT NULL DEFAULT 'WAITING',

            qr_token TEXT UNIQUE,

            joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            called_at TEXT,
            service_started_at TEXT,
            completed_at TEXT,

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
                    'WAITING',
                    'CALLED',
                    'IN_SERVICE',
                    'COMPLETED',
                    'SKIPPED',
                    'NO_SHOW',
                    'CANCELLED',
                    'TRANSFERRED'
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

      System.out.println("Database initialized successfully.");

    } catch (SQLException e) {
      System.err.println("Database initialization failed.");
      e.printStackTrace();
    }
  }
}
