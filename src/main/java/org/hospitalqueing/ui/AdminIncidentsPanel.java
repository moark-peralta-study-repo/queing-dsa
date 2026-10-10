package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.hospitalqueing.dao.IncidentDAO;
import org.hospitalqueing.model.Incident;
import org.hospitalqueing.model.User;

/**
 * Admin section: INCIDENT REPORTS (security-flow expansion). A flat list of every incident ever
 * reported — title, severity, status, reporter, created/resolved stamps — with a filter combo
 * (ALL / OPEN / IN_PROGRESS / RESOLVED), a [+ NEW INCIDENT] modal dialog, and a per-row
 * [Update Status] button that cycles OPEN -> IN_PROGRESS -> RESOLVED (stamping resolvedAt when
 * it reaches RESOLVED).
 */
public class AdminIncidentsPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color WHITE = Color.WHITE;
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color TEXT_MUTED = new Color(113, 128, 150);

  private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final MainFrame parentFrame;
  private final IncidentDAO incidentDAO = new IncidentDAO();

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {"Title", "Severity", "Status", "Reported By", "Created", "Resolved", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private JTable incidentsTable;
  private JComboBox<String> filterCombo;
  private JButton newIncidentButton;

  /** Incident ids aligned to table rows, so the per-row buttons know their target. */
  private final List<Integer> rowIds = new java.util.ArrayList<>();

  public AdminIncidentsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    add(buildHeader(), BorderLayout.NORTH);
    add(buildBody(), BorderLayout.CENTER);

    load();
  }

  /** Dark header bar: "< Back" + "INCIDENT REPORTS" title (left), filter + [+ NEW INCIDENT] (right). */
  private JPanel buildHeader() {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 15 30 15 30, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));

    JLabel titleLabel = new JLabel("INCIDENT REPORTS");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(WHITE);
    headerPanel.add(titleLabel);

    newIncidentButton = new JButton("+ NEW INCIDENT");
    newIncidentButton.setBackground(WHITE);
    newIncidentButton.setForeground(HEADER_DARK_BLUE);
    newIncidentButton.setFocusPainted(false);
    newIncidentButton.setFont(new Font("SansSerif", Font.BOLD, 12));
    newIncidentButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    newIncidentButton.addActionListener(e -> openNewIncidentDialog());

    JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    rightControls.setOpaque(false);
    JLabel filterLabel = new JLabel("Filter:");
    filterLabel.setForeground(new Color(180, 200, 230));
    filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    filterCombo =
        new JComboBox<>(new String[] {"ALL", "OPEN", "IN_PROGRESS", "RESOLVED"});
    filterCombo.setFont(new Font("SansSerif", Font.BOLD, 12));
    filterCombo.setBackground(WHITE);
    filterCombo.addActionListener(e -> load());
    rightControls.add(filterLabel);
    rightControls.add(filterCombo);
    rightControls.add(newIncidentButton);
    headerPanel.add(rightControls);

    return headerPanel;
  }

  /** The table inside a scroll pane that fills the panel (tables stretch to full width). */
  private JComponent buildBody() {
    JPanel scrollArea =
        new JPanel(new MigLayout("insets 0", "[grow, fill]", "[grow, fill]"));
    scrollArea.setBackground(BACKGROUND_LIGHT);

    incidentsTable = new JTable(tableModel);
    incidentsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
    incidentsTable.setRowHeight(38);
    incidentsTable.setGridColor(new Color(230, 230, 230));
    incidentsTable.setSelectionBackground(new Color(227, 242, 253));
    incidentsTable.setSelectionForeground(TEXT_DARK);

    JTableHeader tableHeader = incidentsTable.getTableHeader();
    tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
    tableHeader.setBackground(WHITE);
    tableHeader.setForeground(TEXT_DARK);
    tableHeader.setPreferredSize(new Dimension(100, 40));
    tableHeader.setReorderingAllowed(false);

    JScrollPane tableScroll = new JScrollPane(incidentsTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.getViewport().setBackground(WHITE);
    scrollArea.add(tableScroll);

    return scrollArea;
  }

  /** Re-reads incidents per the active filter (called on open and after any mutation). */
  public void load() {
    tableModel.setRowCount(0);
    rowIds.clear();

    List<Incident> incidents;
    try {
      String filter = (String) filterCombo.getSelectedItem();
      if (filter == null || "ALL".equals(filter)) {
        incidents = incidentDAO.findAll();
      } else {
        incidents = incidentDAO.findByStatus(Incident.Status.valueOf(filter));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    for (Incident incident : incidents) {
      rowIds.add(incident.getId());
      tableModel.addRow(
          new Object[] {
            incident.getTitle() == null ? "" : incident.getTitle(),
            incident.getSeverity() == null ? "--" : incident.getSeverity().name(),
            incident.getStatus() == null ? "--" : incident.getStatus().name(),
            incident.getReportedBy() == null ? "--" : incident.getReportedBy(),
            incident.getCreatedAt() == null ? "--" : incident.getCreatedAt().format(TS_FMT),
            incident.getResolvedAt() == null ? "--" : incident.getResolvedAt().format(TS_FMT)
          });
      int r = tableModel.getRowCount() - 1;
      JButton update = new JButton("Update Status");
      if (incident.getStatus() == Incident.Status.RESOLVED) {
        update.setEnabled(false);
      }
      styleSmallButton(update);
      update.addActionListener(e -> updateStatus(incident));
      tableModel.setValueAt(update, r, 6);
    }
  }

  /** Cycles OPEN -> IN_PROGRESS -> RESOLVED; stamps resolvedAt when it reaches RESOLVED. */
  private void updateStatus(Incident incident) {
    Incident.Status next = Incident.nextStatus(incident.getStatus());
    if (next == null) {
      return;
    }
    try {
      incidentDAO.updateStatus(
          incident.getId(), next, next == Incident.Status.RESOLVED ? LocalDateTime.now() : null);
      load();
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
          this,
          "Could not update status: " + ex.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
      load();
    }
  }

  // ================= NEW INCIDENT DIALOG =================

  private void openNewIncidentDialog() {
    JDialog dialog =
        new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Incident", true);
    JPanel content =
        new JPanel(
            new MigLayout(
                "insets 20 24, wrap 2, gapx 14, gapy 10", "[right][grow 280]"));
    content.setBackground(WHITE);

    JLabel heading = new JLabel("New Incident");
    heading.setFont(new Font("SansSerif", Font.BOLD, 16));
    heading.setForeground(TEXT_DARK);
    content.add(heading, "span 2");

    content.add(new JLabel("Title:"));
    JTextField titleField = new JTextField();
    content.add(titleField);

    content.add(new JLabel("Severity:"));
    JComboBox<String> severityCombo =
        new JComboBox<>(
            new String[] {"LOW", "MEDIUM", "HIGH", "CRITICAL"});
    severityCombo.setSelectedItem("MEDIUM");
    content.add(severityCombo, "wmax 140");

    content.add(new JLabel("Description:"));
    JTextArea descriptionArea = new JTextArea(4, 36);
    descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
    content.add(new JScrollPane(descriptionArea));

    content.add(new JLabel("Reported By:"));
    JTextField reporterField = new JTextField(currentUsername());
    content.add(reporterField);

    JButton saveBtn = new JButton("Save");
    saveBtn.setBackground(new Color(21, 101, 192));
    saveBtn.setForeground(WHITE);
    saveBtn.setFocusPainted(false);
    saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.setBackground(new Color(113, 128, 150));
    cancelBtn.setForeground(WHITE);
    cancelBtn.setFocusPainted(false);
    cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    buttons.setOpaque(false);
    buttons.add(saveBtn);
    buttons.add(cancelBtn);
    content.add(buttons, "span 2, align right, gaptop 6");

    dialog.setContentPane(content);
    dialog.pack();
    dialog.setLocationRelativeTo(this);

    saveBtn.addActionListener(
        e -> {
          String title = titleField.getText().trim();
          if (title.isEmpty()) {
            JOptionPane.showMessageDialog(
                dialog, "Title is required.", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
          }
          Incident incident = new Incident();
          incident.setTitle(title);
          incident.setDescription(descriptionArea.getText().trim());
          incident.setSeverity(Incident.Severity.valueOf((String) severityCombo.getSelectedItem()));
          incident.setStatus(Incident.Status.OPEN);
          incident.setReportedBy(reporterField.getText().trim());
          incident.setCreatedAt(LocalDateTime.now());
          try {
            incidentDAO.insert(incident);
            dialog.dispose();
            filterCombo.setSelectedItem("ALL");
            load();
          } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                dialog,
                "Could not save incident: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
          }
        });
    cancelBtn.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
  }

  /** The logged-in admin's username (prefills the reporter field; blank when not logged in). */
  private String currentUsername() {
    if (parentFrame != null) {
      User u = parentFrame.getLoggedInUser();
      if (u != null && u.getUsername() != null) {
        return u.getUsername();
      }
    }
    return "";
  }

  private void styleSmallButton(JButton b) {
    b.setBackground(b.isEnabled() ? new Color(21, 101, 192) : new Color(170, 180, 195));
    b.setForeground(WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 11));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  // ================= SMOKE-TEST ACCESSORS =================

  public JTable getTable() {
    return incidentsTable;
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }

  public JComboBox<String> getFilterCombo() {
    return filterCombo;
  }

  public JButton getNewIncidentButton() {
    return newIncidentButton;
  }

  /** The [Update Status] button for a table row (column 6), or null. */
  public JButton getUpdateButtonAt(int row) {
    Object cell = tableModel.getValueAt(row, 6);
    return cell instanceof JButton ? (JButton) cell : null;
  }

  /** The incident id backing a table row (aligned list), or -1. */
  public int getRowIdAt(int row) {
    return row >= 0 && row < rowIds.size() ? rowIds.get(row) : -1;
  }

  /** Finds the table row index for the incident with the given title, or -1. */
  public int findRowByTitle(String title) {
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      Object cell = tableModel.getValueAt(i, 0);
      if (cell != null && cell.toString().equals(title)) {
        return i;
      }
    }
    return -1;
  }
}
