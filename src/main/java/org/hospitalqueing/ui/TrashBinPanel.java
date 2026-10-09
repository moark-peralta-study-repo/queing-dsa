package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.QueueEntry;

/**
 * Trash bin (T10): shows soft-deleted (deleted) records — appointments AND queue entries — so the
 * user can Restore them back to live, or permanently delete them for good. Deleting from the
 * history screen routes here (it stamps deleted_at); nothing is hard-removed until the user picks
 * "permanently delete" from this screen.
 */
public class TrashBinPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND_LIGHT = new Color(240, 244, 248);
    private final Color CONTROL_BORDER = new Color(220, 220, 220);
    private final Color DANGER = new Color(194, 42, 56);

    private final MainFrame parentFrame;
    private JTable trashTable;
    private DefaultTableModel tableModel;
    private JButton restoreButton;
    private JButton permanentlyDeleteButton;

    // Aligned with table rows: type (Appointment/Queue) + id, so the buttons know the target.
    private final java.util.List<Ref> refs = new java.util.ArrayList<>();

    private static final class Ref {
        final String type;
        final int id;

        Ref(String type, int id) {
            this.type = type;
            this.id = id;
        }
    }

    public TrashBinPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]10[left]push[right]", "[center]"));
        headerPanel.setBackground(WHITE);

        JLabel backBtn = new JLabel("⬅");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        backBtn.setForeground(TEXT_DARK);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("HISTORY_PAGE");
            }
        });

        JLabel logoLabel = new JLabel("🗑");
        logoLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        JLabel titleLabel = new JLabel("HOSPITAL - TRASH BIN");
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        headerPanel.add(backBtn);
        headerPanel.add(logoLabel);
        headerPanel.add(titleLabel);

        // Action buttons top-right
        restoreButton = new JButton("Restore");
        styleButton(restoreButton, PRIMARY_BLUE);
        permanentlyDeleteButton = new JButton("Permanently Delete");
        styleButton(permanentlyDeleteButton, DANGER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(restoreButton);
        actions.add(permanentlyDeleteButton);
        headerPanel.add(actions);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(new JSeparator(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- TABLE CARD ---
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setBackground(BACKGROUND_LIGHT);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel pageTitle = new JLabel("Deleted Records");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        pageTitle.setForeground(TEXT_DARK);
        JLabel pageSub = new JLabel("Restore to bring a record back, or permanently delete to remove it for good.");
        pageSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pageSub.setForeground(new Color(110, 122, 140));
        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(pageTitle, BorderLayout.NORTH);
        titleBlock.add(pageSub, BorderLayout.SOUTH);
        centerWrapper.add(titleBlock, BorderLayout.NORTH);

        String[] columns = {"Type", "Department", "Doctor", "Status", "Deleted At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trashTable = new JTable(tableModel);
        trashTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        trashTable.setRowHeight(38);
        trashTable.setGridColor(new Color(230, 230, 230));
        trashTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trashTable.setSelectionBackground(new Color(227, 242, 253));
        trashTable.setSelectionForeground(TEXT_DARK);

        JTableHeader tableHeader = trashTable.getTableHeader();
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        tableHeader.setBackground(WHITE);
        tableHeader.setForeground(TEXT_DARK);
        tableHeader.setPreferredSize(new Dimension(100, 40));

        JScrollPane tableScroll = new JScrollPane(trashTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER));
        tableScroll.getViewport().setBackground(WHITE);
        centerWrapper.add(tableScroll, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        restoreButton.addActionListener(e -> doAction("restore"));
        permanentlyDeleteButton.addActionListener(e -> doAction("permanent"));
    }

    /** Re-reads the trash on every open (MainFrame calls this) and after any action. */
    public void loadTrashData() {
        tableModel.setRowCount(0);
        refs.clear();
        try {
            for (Appointment a : new AppointmentDAO().findTrashed()) {
                String deptName = UiData.departmentNameForService(a.getServiceId());
                String doctorName = a.getDoctorId() != null ? UiData.doctorName(a.getDoctorId()) : "--";
                tableModel.addRow(new Object[]{
                        "Appointment",
                        deptName == null ? "--" : deptName,
                        doctorName == null ? "--" : doctorName,
                        a.getStatus() == null ? "--" : a.getStatus(),
                        a.getDeletedAt() == null ? "--" : a.getDeletedAt()});
                refs.add(new Ref("Appointment", a.getAppointmentId()));
            }
            for (QueueEntry q : new QueueEntryDAO().findTrashed()) {
                String deptName = q.getDepartmentId() > 0 ? UiData.departmentName(q.getDepartmentId()) : null;
                String doctorName = q.getDoctorId() != null ? UiData.doctorName(q.getDoctorId()) : "--";
                tableModel.addRow(new Object[]{
                        "Queue",
                        deptName == null ? "--" : deptName,
                        doctorName == null ? "--" : doctorName,
                        q.getStatus() == null ? "--" : q.getStatus(),
                        q.getDeletedAt() == null ? "--" : q.getDeletedAt()});
                refs.add(new Ref("Queue", q.getQueueId()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doAction(String kind) {
        int viewRow = trashTable.getSelectedRow();
        int modelRow = viewRow < 0 ? -1 : trashTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= refs.size()) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "Trash Bin", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Ref ref = refs.get(modelRow);
        if ("restore".equals(kind)) {
            try {
                restore(ref);
                loadTrashData();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Could not restore: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this,
                "Permanently delete this " + ref.type.toLowerCase() + "? This cannot be undone.",
                "Permanently Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            permanentDelete(ref);
            loadTrashData();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restore(Ref ref) {
        if ("Queue".equals(ref.type)) {
            new org.hospitalqueing.controller.QueueController(
                    new org.hospitalqueing.service.QueueService(new QueueEntryDAO())).restore(ref.id);
        } else {
            new org.hospitalqueing.controller.AppointmentController(
                    new org.hospitalqueing.service.AppointmentService(new AppointmentDAO()))
                    .restoreAppointment(ref.id);
        }
    }

    private void permanentDelete(Ref ref) {
        if ("Queue".equals(ref.type)) {
            new org.hospitalqueing.controller.QueueController(
                    new org.hospitalqueing.service.QueueService(new QueueEntryDAO())).permanentlyDelete(ref.id);
        } else {
            new org.hospitalqueing.controller.AppointmentController(
                    new org.hospitalqueing.service.AppointmentService(new AppointmentDAO()))
                    .permanentlyDeleteAppointment(ref.id);
        }
    }

    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(150, 34));
    }

    // ================= SMOKE-TEST ACCESSORS =================

    public JTable getTrashTable() {
        return trashTable;
    }

    public JButton getRestoreButton() {
        return restoreButton;
    }

    public JButton getPermanentlyDeleteButton() {
        return permanentlyDeleteButton;
    }
}
