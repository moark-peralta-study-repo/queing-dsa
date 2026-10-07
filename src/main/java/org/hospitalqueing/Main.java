package org.hospitalqueing;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.database.SeedDemoData;
import org.hospitalqueing.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize SQLite database tables securely before loading the UI
        DatabaseConnection.initializeDatabase();

        // 2. Seed demo users/departments/services/doctors when the DB is empty, so the system
        //    works out of the box (logins: patient/patient, staff/staff, admin/admin).
        SeedDemoData.seedIfEmpty();

        // 3. Setup FlatLaf for the modern UI aesthetic
        FlatLightLaf.setup();

        // 4. Launch the MainFrame window safely on the Swing event dispatch thread
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
