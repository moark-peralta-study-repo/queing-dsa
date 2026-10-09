package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.Patient;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.User;

/**
 * Patient-facing history screen (T5): lists the logged-in patient's appointments AND queue
 * entries in one table, with a filter bar on top — date range (start/end, both optional:
 * leave either blank to bound only one side), record type (All / Queue / Appointment),
 * department, and patient-name free text (case-insensitive substring match).
 *
 * <p>Re-querying happens on {@link #loadHistoryData()} (MainFrame calls it every time the
 * screen opens) and on the filter bar's Refresh button; filter field changes alone do not
 * trigger a query, matching the rest of the app's click-driven style.
 */
public class AppointmentHistoryPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND_LIGHT = new Color(240, 244, 248);
    private final Color CONTROL_BORDER = new Color(220, 220, 220);

    private JLabel backBtn;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private MainFrame parentFrame;
    // Aligned with table rows: each entry is a small holder for the record's type + id,
    // so the Delete button in the Actions column knows exactly which row to soft-delete.
    private final java.util.List<RowRef> rowRefs = new java.util.ArrayList<>();

    // Filter bar controls
    private JComponent filterPanel;
    private JTextField startField;
    private JTextField endField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> departmentCombo;
    private JTextField nameField;
    private JButton refreshBtn;

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

        JLabel trashLink = new JLabel("🗑 Trash");
        trashLink.setFont(new Font("SansSerif", Font.BOLD, 14));
        trashLink.setForeground(TEXT_DARK);
        trashLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        trashLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("TRASH_BIN");
            }
            public void mouseEntered(java.awt.event.MouseEvent e) { trashLink.setForeground(PRIMARY_BLUE); }
            public void mouseExited(java.awt.event.MouseEvent e) { trashLink.setForeground(TEXT_DARK); }
        });
        headerPanel.add(trashLink);

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
        String[] columns = {"Date", "Department", "Doctor", "Type", "Status", "Actions"};
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
        tableScroll.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER));
        tableScroll.getViewport().setBackground(WHITE);

        // --- 2a. DELETE (soft-delete) action in the Actions column ---
        int actionsCol = tableModel.getColumnCount() - 1;
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        deleteButton.setForeground(WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            int viewRow = historyTable.getSelectedRow();
            int modelRow = viewRow < 0 ? -1 : historyTable.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= rowRefs.size()) {
                return;
            }
            RowRef ref = rowRefs.get(modelRow);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Move this " + ref.type.toLowerCase() + " to the trash bin? You can restore it later from the Trash.",
                    "Delete Record", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opt != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                if ("Queue".equals(ref.type)) {
                    new org.hospitalqueing.controller.QueueController(
                            new org.hospitalqueing.service.QueueService(new QueueEntryDAO())).delete(ref.id);
                } else {
                    new org.hospitalqueing.controller.AppointmentController(
                            new org.hospitalqueing.service.AppointmentService(new AppointmentDAO()))
                            .deleteAppointment(ref.id);
                }
                loadHistoryData();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Could not delete: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        javax.swing.table.DefaultTableCellRenderer buttonRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                if (col == actionsCol) {
                    deleteButton.setBackground(isSelected ? new Color(18, 90, 170) : PRIMARY_BLUE);
                    return deleteButton;
                }
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        };
        historyTable.setDefaultRenderer(Object.class, buttonRenderer);

        // --- 2b. FILTER BAR (above the table, inside the same card) ---
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(BACKGROUND_LIGHT);
        bodyPanel.add(buildFilterBar(), BorderLayout.NORTH);
        bodyPanel.add(tableScroll, BorderLayout.CENTER);
        centerWrapper.add(bodyPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("DASHBOARD_PAGE");
            }
        });
    }

    // ================= FILTER BAR =================

    private JComponent buildFilterBar() {
        String[] deptNames = UiData.departmentNames();
        String[] deptItems = new String[deptNames.length + 1];
        deptItems[0] = "All Departments";
        System.arraycopy(deptNames, 0, deptItems, 1, deptNames.length);

        typeCombo = new JComboBox<>(new String[]{"All", "Queue", "Appointment"});
        departmentCombo = new JComboBox<>(deptItems);
        startField = new JTextField(10);
        endField = new JTextField(10);
        nameField = new JTextField(10);
        startField.putClientProperty("JTextField.placeholderText", "From YYYY-MM-DD");
        endField.putClientProperty("JTextField.placeholderText", "To YYYY-MM-DD");
        nameField.putClientProperty("JTextField.placeholderText", "Patient name");

        Font controlFont = new Font("SansSerif", Font.PLAIN, 13);
        for (JComponent c : new JComponent[]{typeCombo, departmentCombo, startField, endField, nameField}) {
            c.setFont(controlFont);
            c.setBackground(WHITE);
            c.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER));
        }
        typeCombo.setPreferredSize(new Dimension(150, 34));
        departmentCombo.setPreferredSize(new Dimension(190, 34));
        refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(PRIMARY_BLUE);
        refreshBtn.setForeground(WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(100, 34));

        JPanel panel = new JPanel(new MigLayout(
                "insets 14 20 14 20, gap 10 14, fillx",
                "[left, grow, fill]"));
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        // Row 1: date range + record type
        panel.add(new JLabel("Date:"), "align right");
        panel.add(startField, "w 140!");
        panel.add(new JLabel("–"), "align center");
        panel.add(endField, "w 140!");
        panel.add(new JLabel("Type:"), "align right");
        panel.add(typeCombo, "w 140!, wrap");
        // Row 2: department + patient name + refresh
        panel.add(new JLabel("Department:"), "align right");
        panel.add(departmentCombo, "w 180!");
        panel.add(new JLabel("Patient:"), "align right");
        panel.add(nameField, "w 140!");
        panel.add(refreshBtn, "w 100!");
        refreshBtn.addActionListener(e -> loadHistoryData());
        filterPanel = panel;
        return panel;
    }

    // ================= DATA LOADING =================

    /**
     * Called by MainFrame every time this screen opens, and by the Refresh button, to load
     * fresh filtered data.
     */
    public void loadHistoryData() {
        if (parentFrame == null) {
            return;
        }
        // Always refresh the table on a clean slate; if there's no user yet, show nothing.
        tableModel.setRowCount(0);
        rowRefs.clear();

        User user = parentFrame.getLoggedInUser();
        if (user == null) {
            return;
        }

        Patient patient = UiData.patientProfileForUser(user);
        if (patient == null) {
            // No patient profile linked to this account; nothing to show.
            return;
        }
        int patientId = patient.getPatientId();

        LocalDate from = parseFilterDate(startField);
        LocalDate to = parseFilterDate(endField);
        String type = (String) typeCombo.getSelectedItem();
        String deptName = (String) departmentCombo.getSelectedItem();
        int deptId = (deptName == null || "All Departments".equals(deptName))
                ? -1 : UiData.departmentIdByName(deptName);
        String nameQ = nameField.getText().trim().toLowerCase();

        try {
            List<HistoryRow> rows = new ArrayList<>();
            rows.addAll(queryAppointments(patientId, from, to, type, deptId));
            rows.addAll(queryQueueEntries(patientId, from, to, type, deptId));

            if (!nameQ.isEmpty()) {
                String displayName = patientName(patient);
                List<HistoryRow> kept = new ArrayList<>();
                for (HistoryRow r : rows) {
                    if (displayName.toLowerCase().contains(nameQ)) {
                        kept.add(r);
                    }
                }
                rows = kept;
            }

            rows.sort((a, b) -> a.dateStr.compareTo(b.dateStr));

            for (HistoryRow r : rows) {
                tableModel.addRow(new Object[]{r.dateStr, r.deptName, r.doctorName, r.type, r.status, r.type + " #" + r.id});
                rowRefs.add(new RowRef(r.type, r.id));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** A merged (appointment | queue-entry) table row. */
    private static final class HistoryRow {
        String dateStr;
        String deptName;
        String doctorName;
        String type;
        String status;
        int id;

        HistoryRow(String dateStr, String deptName, String doctorName, String type, String status, int id) {
            this.dateStr = dateStr;
            this.deptName = deptName;
            this.doctorName = doctorName;
            this.type = type;
            this.status = status;
            this.id = id;
        }
    }

    /** Record type + id of a history row, so Delete/soft-delete knows exactly which row to act on. */
    private static final class RowRef {
        final String type;
        final int id;

        RowRef(String type, int id) {
            this.type = type;
            this.id = id;
        }
    }

    /** Queries appointments for the patient, applying the active date/type/department filters. */
    private List<HistoryRow> queryAppointments(int patientId, LocalDate from, LocalDate to,
                                               String type, int deptId) {
        List<HistoryRow> out = new ArrayList<>();
        if ("Queue".equals(type)) {
            return out;
        }
        for (Appointment appt : new AppointmentDAO().findByPatient(patientId)) {
            LocalDate d = appt.getAppointmentDate();
            if (d == null || (from != null && d.isBefore(from)) || (to != null && d.isAfter(to))) {
                continue;
            }
            if (deptId > 0 && serviceDepartmentId(appt.getServiceId()) != deptId) {
                continue;
            }
            String deptName = appt.getServiceId() > 0 ? UiData.departmentNameForService(appt.getServiceId()) : null;
            if (deptName == null) deptName = "--";
            String doctorName = appt.getDoctorId() != null ? UiData.doctorName(appt.getDoctorId()) : "--";
            if (doctorName == null) doctorName = "--";
            String status = appt.getStatus() == null ? "--" : appt.getStatus();
            out.add(new HistoryRow(d.toString(), deptName, doctorName, "Appointment", status, appt.getAppointmentId()));
        }
        return out;
    }

    /** Queries queue entries for the patient, applying the active date/type/department filters. */
    private List<HistoryRow> queryQueueEntries(int patientId, LocalDate from, LocalDate to,
                                               String type, int deptId) {
        List<HistoryRow> out = new ArrayList<>();
        if ("Appointment".equals(type)) {
            return out;
        }
        for (QueueEntry q : new QueueEntryDAO().findByPatient(patientId)) {
            LocalDate d;
            try {
                d = q.getQueueDate() != null ? LocalDate.parse(q.getQueueDate()) : null;
            } catch (Exception ex) {
                d = null;
            }
            if (d == null || (from != null && d.isBefore(from)) || (to != null && d.isAfter(to))) {
                continue;
            }
            if (deptId > 0 && q.getDepartmentId() != deptId) {
                continue;
            }
            String deptName = q.getDepartmentId() > 0 ? UiData.departmentName(q.getDepartmentId()) : null;
            if (deptName == null) deptName = "--";
            String doctorName = q.getDoctorId() != null ? UiData.doctorName(q.getDoctorId()) : "--";
            if (doctorName == null) doctorName = "--";
            String status = q.getStatus() == null ? "--" : q.getStatus();
            out.add(new HistoryRow(d.toString(), deptName, doctorName, "Queue", status, q.getQueueId()));
        }
        return out;
    }

    /** Resolves the department id for a service, or -1 when the service row is gone. */
    private static int serviceDepartmentId(int serviceId) {
        if (serviceId <= 0) {
            return -1;
        }
        org.hospitalqueing.model.Service s =
                new org.hospitalqueing.dao.ServiceDAO().findById(serviceId);
        return s == null ? -1 : s.getDepartmentId();
    }

    /** Display name ("First Last") for a patient row, falling back to "Patient #id". */
    private static String patientName(Patient patient) {
        String name = (patient.getFirstName() == null ? "" : patient.getFirstName().trim())
                + " "
                + (patient.getLastName() == null ? "" : patient.getLastName().trim());
        return name.isBlank() ? "Patient #" + patient.getPatientId() : name.trim();
    }

    /** Parses a filter date field (YYYY-MM-DD); empty or malformed fields mean "no bound". */
    private static LocalDate parseFilterDate(JTextField field) {
        if (field == null) {
            return null;
        }
        String s = field.getText().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (Exception ex) {
            return null; // Invalid bound is treated as no bound rather than crashing the query.
        }
    }

    // ================= SMOKE-TEST ACCESSORS =================

    /** The history JTable (for verification harnesses). */
    public JTable getHistoryTable() {
        return historyTable;
    }

    /** The filter bar container (so a harness can reach the filter fields). */
    public JComponent getFilterPanel() {
        return filterPanel;
    }
}
