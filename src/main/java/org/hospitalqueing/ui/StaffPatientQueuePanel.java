package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueStatus;
import org.hospitalqueing.service.QueueManagementService;

/**
 * Staff "Live Patient Queue" panel.
 *
 * <p>Backed by {@link QueueManagementService} (not dummy rows). Each table row carries the queue
 * entry's id in a parallel list so the action panel (call / update status / no-show) can operate on
 * the real database record. The table refreshes itself after every action, and the panel also
 * exposes {@link #refresh()} so it can be re-queried whenever the screen is shown.
 *
 * <p>Column layout: [Queue #, Department, Patient, Joined, Status]
 */
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

    // Real queue service, built once.
    private final QueueManagementService qms;
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    // Queue entry ids for each currently displayed row (kept in sync with the table).
    private final List<Integer> rowQueueIds = new ArrayList<>();

    public StaffPatientQueuePanel() {
        this.qms = new QueueManagementService(
                new QueueEntryDAO(),
                new QueueEventDAO(),
                new ServiceDAO(),
                departmentDAO,
                new CounterDAO());

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

        String[] columns = {"Queue No.", "Department", "Patient Name", "Time", "Status"};
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

        // Status Updater — values MUST match the queue_entries.status CHECK constraint, which is
        // now the panel's clinical vocabulary (see QueueStatus). The full set is offered so the
        // combo can always reflect a ticket's real current state; the workflow transitions
        // (Waiting -> Checked In -> In Consultation -> terminal) are still driven by the buttons.
        String[] statuses = {
            QueueStatus.WAITING,
            QueueStatus.CHECKED_IN,
            QueueStatus.IN_CONSULTATION,
            QueueStatus.FOR_LABORATORY,
            QueueStatus.FOR_PHARMACY,
            QueueStatus.DISCHARGED,
            QueueStatus.COMPLETED,
            QueueStatus.NO_SHOW
        };
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

        // Populate from the live queue (not dummy data).
        refresh();

        // When a row is clicked, update the action panel labels.
        queueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && queueTable.getSelectedRow() != -1) {
                int row = queueTable.getSelectedRow();
                selectedQueueNoLbl.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                selectedPatientLbl.setText((String) tableModel.getValueAt(row, 2));

                // Sync the combo to the row's current status when it is one of the editable
                // (non-terminal) states; otherwise leave the operator's previous choice alone.
                String currentStatus = (String) tableModel.getValueAt(row, 4);
                if (currentStatus != null && UiData.indexOfItem(statusCombo, currentStatus) >= 0) {
                    statusCombo.setSelectedItem(currentStatus);
                }
            }
        });

        // Actions — all hit the real service, then refresh the table.
        callPatientBtn.addActionListener(e -> {
            int qid = selectedQueueId();
            if (qid < 0) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the list first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                boolean moved = qms.advance(qid, null);
                QueueEntry after = qms.getEntry(qid);
                if (!moved) {
                    JOptionPane.showMessageDialog(this, "This patient is already at a terminal state and cannot be advanced further.", "Call Patient", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Patient advanced.\nNew status: " + (after != null ? after.getStatus() : "(see table)"),
                            "Call Patient", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error calling patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
        });

        updateStatusBtn.addActionListener(e -> {
            int qid = selectedQueueId();
            if (qid < 0) return;
            String target = (String) statusCombo.getSelectedItem();
            if (target == null) return;
            try {
                boolean ok = qms.setStatus(qid, target);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Could not set status. The entry may already be in that state or is invalid for this transition.", "Update Status", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
        });

        noShowBtn.addActionListener(e -> {
            int qid = selectedQueueId();
            if (qid < 0) return;
            try {
                boolean ok = qms.markNoShow(qid);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Could not mark as no-show (entry missing or already terminal).", "No Show", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error marking no-show: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
        });
    }

    private int selectedQueueId() {
        int row = queueTable.getSelectedRow();
        if (row < 0 || row >= rowQueueIds.size()) return -1;
        return rowQueueIds.get(row);
    }

    /**
     * Rebuilds the table from the live active queue across all departments. Called on panel
     * construction and after every action so the operator always sees the current state. Safe to
     * call again from {@code MainFrame.showScreen} whenever this card is displayed.
     */
    public void refresh() {
        if (tableModel == null) return; // guard against very early calls

        tableModel.setRowCount(0);
        rowQueueIds.clear();

        try {
            // Union of active queues across all departments. Enumerating departments (rather than
            // hard-coding them) means a freshly seeded DB is picked up without a code change.
            List<Department> departments = departmentDAO.findAll();
            for (Department d : departments) {
                List<QueueEntry> active = qms.getActiveQueue(d.getDepartmentId());
                for (QueueEntry qe : active) {
                    Object[] row = new Object[] {
                            UiData.queueLabel(d.getDepartmentName(), qe.getQueueNumber()),
                            d.getDepartmentName(),
                            UiData.patientName(qe.getPatientId()),
                            formatJoinedAt(qe.getJoinedAt()),
                            qe.getStatus()
                    };
                    tableModel.addRow(row);
                    rowQueueIds.add(qe.getQueueId());
                }
            }
        } catch (Exception ex) {
            // A single failed read shouldn't take the table down.
            ex.printStackTrace();
        }

        // Reset the action panel so it doesn't point at a stale row.
        selectedQueueNoLbl.setText("Select a patient");
        selectedPatientLbl.setText("--");
    }

    private static String formatJoinedAt(String joinedAt) {
        if (joinedAt == null || joinedAt.isBlank()) return "--";
        // joined_at looks like "2026-10-06 09:42:11" — trim to HH:MM for display.
        int space = joinedAt.indexOf(' ');
        if (space < 0) return joinedAt;
        String time = joinedAt.substring(space + 1);
        int colon = time.indexOf(':');
        if (colon > 0 && time.length() >= colon + 3) {
            return time.substring(0, colon) + ":" + time.substring(colon + 1, colon + 3);
        }
        return time;
    }
}
