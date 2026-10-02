package org.hospitalqueing;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize SQLite database tables securely before loading the UI
        DatabaseConnection.initializeDatabase();

        // 2. Setup FlatLaf for the modern UI aesthetic
        FlatLightLaf.setup();
        
        // 3. Launch the MainFrame window safely on the Swing event dispatch thread
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}