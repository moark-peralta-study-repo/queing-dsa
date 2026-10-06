package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class StaffPatientQueuePanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color DANGER_RED = new Color(231, 76, 60);

    private JTable queueTable;
    private DefaultTableModel tableModel;
    
    // Action Panel Components
    private JLabel selectedPatientLbl;
    private JLabel selectedQueueNoLbl;
    private JComboBox<String> statusCombo;
    private JButton callPatientBtn;
    private JButton updateStatusBtn;
    private JButton noShowBtn;

    public StaffPatientQueuePanel() {
        setLayout(new BorderLayout(20, 0));
        setBackground(BACKGROUND_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- LEFT SIDE: QUEUE TABLE ---
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(BACKGROUND_LIGHT);

        JLabel tableTitle = new JLabel("Live Patient Queue");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        tableTitle.setForeground(TEXT_DARK);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Queue No.", "Patient Name", "Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        queueTable = new JTable(tableModel);
        queueTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        queueTable.setRowHeight(35);
        queueTable.setSelectionBackground(new Color(227, 242, 253));
        queueTable.setSelectionForeground(TEXT_DARK);

        JTableHeader tableHeader = queueTable.getTableHeader();
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        tableHeader.setBackground(WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 40));

        JScrollPane tableScroll = new JScrollPane(queueTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tableScroll.getViewport().setBackground(WHITE);
        
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        // --- RIGHT SIDE: ACTION CONTROLS ---
        JPanel actionContainer = new JPanel(new MigLayout("wrap 1, insets 30, fillx, top", "[center, fill]", "[]10[]30[]10[]15[]30[]10[]"));
        actionContainer.setBackground(WHITE);
        actionContainer.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        actionContainer.setPreferredSize(new Dimension(350, 0));

        JLabel actionTitle = new JLabel("Patient Controls");
        actionTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        actionTitle.setForeground(TEXT_DARK);

        // Selected Patient Info
        selectedQueueNoLbl = new JLabel("Select a patient");
        selectedQueueNoLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        selectedQueueNoLbl.setForeground(PRIMARY_BLUE);
        
        selectedPatientLbl = new JLabel("--");
        selectedPatientLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        selectedPatientLbl.setForeground(TEXT_DARK);

        // Big Call Button
        callPatientBtn = new JButton("📢 Call Next / Selected");
        callPatientBtn.setBackground(PRIMARY_BLUE);
        callPatientBtn.setForeground(WHITE);
        callPatientBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        callPatientBtn.setFocusPainted(false);
        callPatientBtn.setPreferredSize(new Dimension(0, 50));
        callPatientBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Status Updater
        String[] statuses = {"Checked In", "In Consultation", "For Laboratory", "For Pharmacy", "Discharged", "Completed"};
        statusCombo = new JComboBox<>(statuses);
        statusCombo.setBackground(WHITE);
        statusCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusCombo.setPreferredSize(new Dimension(0, 40));

        updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.setBackground(SUCCESS_GREEN);
        updateStatusBtn.setForeground(WHITE);
        updateStatusBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateStatusBtn.setFocusPainted(false);
        updateStatusBtn.setPreferredSize(new Dimension(0, 40));
        updateStatusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        noShowBtn = new JButton("Mark as No Show");
        noShowBtn.setBackground(WHITE);
        noShowBtn.setForeground(DANGER_RED);
        noShowBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        noShowBtn.setFocusPainted(false);
        noShowBtn.setBorder(BorderFactory.createLineBorder(DANGER_RED, 1));
        noShowBtn.setPreferredSize(new Dimension(0, 40));
        noShowBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        actionContainer.add(actionTitle, "gapbottom 20");
        actionContainer.add(selectedQueueNoLbl);
        actionContainer.add(selectedPatientLbl, "gapbottom 20");
        actionContainer.add(callPatientBtn, "gapbottom 30");
        
        actionContainer.add(new JLabel("Change Patient Status:"), "left");
        actionContainer.add(statusCombo);
        actionContainer.add(updateStatusBtn, "gapbottom 20");
        
        actionContainer.add(noShowBtn, "gaptop 20");

        // Add both to main layout
        add(tableContainer, BorderLayout.CENTER);
        add(actionContainer, BorderLayout.EAST);

        // --- UI WIRING ---
        
        // Populate Dummy Data (Teammate will replace this with DB call)
        loadDummyQueueData();

        // When a row is clicked, update the action panel labels
        queueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && queueTable.getSelectedRow() != -1) {
                int row = queueTable.getSelectedRow();
                selectedQueueNoLbl.setText((String) tableModel.getValueAt(row, 0));
                selectedPatientLbl.setText((String) tableModel.getValueAt(row, 1));
                
                String currentStatus = (String) tableModel.getValueAt(row, 3);
                statusCombo.setSelectedItem(currentStatus);
            }
        });

        // Button Actions (Teammate will add MVC calls here)
        callPatientBtn.addActionListener(e -> {
            if (queueTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the list first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // TODO for Teammate: Send Notification to Patient via NotificationDAO, update Queue Status to "Being Served"
            JOptionPane.showMessageDialog(this, "Calling Patient: " + selectedPatientLbl.getText());
        });

        updateStatusBtn.addActionListener(e -> {
            if (queueTable.getSelectedRow() == -1) return;
            // TODO for Teammate: Update patient transaction status in database
            JOptionPane.showMessageDialog(this, "Status updated to: " + statusCombo.getSelectedItem());
        });

        noShowBtn.addActionListener(e -> {
            if (queueTable.getSelectedRow() == -1) return;
            // TODO for Teammate: Remove from active queue or mark as abandoned in DB
            JOptionPane.showMessageDialog(this, selectedPatientLbl.getText() + " marked as No Show.");
        });
    }

    private void loadDummyQueueData() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"A-022", "Juan Dela Cruz", "9:00 AM", "In Consultation"});
        tableModel.addRow(new Object[]{"A-023", "Maria Santos", "9:30 AM", "Waiting"});
        tableModel.addRow(new Object[]{"A-024", "Carlo Reyes", "9:45 AM", "Waiting"});
    }
}