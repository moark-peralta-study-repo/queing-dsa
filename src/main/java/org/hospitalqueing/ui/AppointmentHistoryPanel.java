package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.User;

public class AppointmentHistoryPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND_LIGHT = new Color(240, 244, 248);

    private JLabel backBtn;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private MainFrame parentFrame;

    public AppointmentHistoryPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- 1. HEADER ---
        JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]10[left]push", "[center]"));
        headerPanel.setBackground(WHITE);

        backBtn = new JLabel("⬅");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        backBtn.setForeground(TEXT_DARK);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel logoLabel = new JLabel("✚");
        logoLabel.setForeground(PRIMARY_BLUE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JLabel titleLabel = new JLabel("HOSPITAL - APPOINTMENT HISTORY");
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        headerPanel.add(backBtn);
        headerPanel.add(logoLabel);
        headerPanel.add(titleLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(new JSeparator(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. TABLE BODY CARD ---
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setBackground(BACKGROUND_LIGHT);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel pageTitle = new JLabel("Your Transactions & Appointments");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        pageTitle.setForeground(TEXT_DARK);
        centerWrapper.add(pageTitle, BorderLayout.NORTH);

        // Define Table Columns
        String[] columns = {"Date", "Department", "Doctor", "Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent user from typing inside the table cells
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        historyTable.setRowHeight(35);
        historyTable.setGridColor(new Color(230, 230, 230));
        historyTable.setSelectionBackground(new Color(227, 242, 253));
        historyTable.setSelectionForeground(TEXT_DARK);

        // Style the Table Header
        JTableHeader tableHeader = historyTable.getTableHeader();
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        tableHeader.setBackground(WHITE);
        tableHeader.setForeground(TEXT_DARK);
        tableHeader.setPreferredSize(new Dimension(100, 40));

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tableScroll.getViewport().setBackground(WHITE);

        centerWrapper.add(tableScroll, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("DASHBOARD_PAGE");
            }
        });
    }

    // Called by MainFrame every time this screen opens to load fresh data
    public void loadHistoryData() {
        tableModel.setRowCount(0); // Clear existing rows

        if (parentFrame.getLoggedInUser() == null) {
            return;
        }

        User user = parentFrame.getLoggedInUser();
        int patientId = UiData.patientIdForUser(user);
        if (patientId < 0) {
            // No patient profile linked to this account; nothing to show.
            return;
        }

        try {
            AppointmentDAO dao = new AppointmentDAO();
            List<Appointment> appointments = dao.findByPatient(patientId);

            for (Appointment appt : appointments) {
                String date = appt.getAppointmentDate() != null ? appt.getAppointmentDate().toString() : "--";
                String deptName = UiData.departmentNameForService(appt.getServiceId());
                String doctorName = appt.getDoctorId() != null ? UiData.doctorName(appt.getDoctorId()) : "--";
                String type = "Appointment";
                String status = appt.getStatus() == null ? "--" : appt.getStatus();

                tableModel.addRow(new Object[]{date, deptName, doctorName, type, status});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}