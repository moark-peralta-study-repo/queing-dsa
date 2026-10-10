package org.hospitalqueing.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

import net.miginfocom.swing.MigLayout;
import org.hospitalqueing.dao.SecurityLogDAO;
import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.SecurityLog;

/**
 * Admin section: SECURITY LOGS — the auth-event history (login successes, login failures and
 * logouts) recorded by the login/logout handlers, with an action filter (ALL / LOGIN SUCCESS /
 * LOGIN FAIL / LOGOUT) plus a username filter and an Apply button.
 *
 * <p>Rows load on construction and again on Apply, always newest-first. The Result column colors
 * green for success, red for failure (logout shows "Logged out" in blue).
 */
public class AdminSecurityLogsPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color WHITE = Color.WHITE;
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color TEXT_MUTED = new Color(113, 128, 150);
  private static final Color OK_GREEN = new Color(46, 125, 50);
  private static final Color FAIL_RED = new Color(214, 64, 64);

  /** Filter combo values (the display labels double as the action filter; ALL = no filter). */
  private static final String[] ACTION_FILTERS =
      {"ALL", "LOGIN SUCCESS", "LOGIN FAIL", "LOGOUT"};

  private final SecurityLogDAO dao = new SecurityLogDAO(DatabaseConnection.getSingleton());

  private final JComboBox<String> actionFilter = new JComboBox<>(ACTION_FILTERS);
  private final JTextField usernameFilter = new JTextField();
  private JButton applyButton;
  private JTable logTable;

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {
            "Action", "Username", "Role", "Result", "Host", "Time"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

  public AdminSecurityLogsPanel(MainFrame parentFrame) {
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildHeader(parentFrame), BorderLayout.NORTH);
    add(buildBody(), BorderLayout.CENTER);

    // Load once on construction so the panel is live the moment it is shown.
    loadLogs();
  }

  /** Dark header bar: "< Back" to the admin dashboard (left) + "SECURITY LOGS" title (bold 20). */
  private JPanel buildHeader(MainFrame parentFrame) {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 15 30 15 30, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
    left.setOpaque(false);
    left.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));
    JLabel titleLabel = new JLabel("SECURITY LOGS");
    titleLabel.setForeground(WHITE);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    left.add(titleLabel);

    headerPanel.add(left, BorderLayout.WEST);
    return headerPanel;
  }

  /** Filter row + the log table in a scroll pane filling the rest of the panel (MigLayout grow). */
  private JPanel buildBody() {
    JPanel body =
        new JPanel(
            new MigLayout(
                "insets 15 30 15 30, fillx, wrap",
                "[grow, fill]",
                "[]10[grow 300, fill]"));
    body.setOpaque(false);

    // --- Filter row: action combo + username field + Apply ---
    JPanel filterRow =
        new JPanel(
            new MigLayout(
                "insets 10 14, wrap, aligny center",
                "[right]10[220]10[right]10[300, grow]10[grow]",
                "[]"));
    filterRow.setBackground(WHITE);
    filterRow.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));

    JLabel actionLabel = new JLabel("Action:");
    actionLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    actionLabel.setForeground(TEXT_DARK);
    filterRow.add(actionLabel);
    actionFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
    filterRow.add(actionFilter);

    JLabel usernameLabel = new JLabel("Username:");
    usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    usernameLabel.setForeground(TEXT_DARK);
    filterRow.add(usernameLabel);
    usernameFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
    usernameFilter.putClientProperty("JTextField.placeholderText", "filter by username (optional)");
    filterRow.add(usernameFilter);

    JButton applyBtn = new JButton("Apply");
    applyBtn.setFocusPainted(false);
    applyBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    applyBtn.setBackground(new Color(21, 101, 192));
    applyBtn.setForeground(WHITE);
    applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    applyBtn.addActionListener(e -> loadLogs());
    this.applyButton = applyBtn;
    filterRow.add(applyBtn);
    body.add(filterRow);

    // --- Log table ---
    logTable = new JTable(tableModel);
    logTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
    logTable.setRowHeight(30);
    logTable.setGridColor(new Color(230, 230, 230));
    logTable.setSelectionBackground(new Color(227, 242, 253));
    logTable.setSelectionForeground(TEXT_DARK);
    logTable.setRowSelectionAllowed(true);
    logTable.setDefaultRenderer(
        String.class,
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (column == 3) { // Result: colored (green ok / red fail / blue logout)
              String s = value == null ? "" : value.toString();
              c.setForeground(
                  s.contains("fail") ? FAIL_RED : s.contains("out") ? new Color(21, 101, 192) : OK_GREEN);
              c.setFont(new Font("SansSerif", Font.BOLD, 13));
            }
            return c;
          }
        });

    JTableHeader tableHeader = logTable.getTableHeader();
    tableHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
    tableHeader.setBackground(WHITE);
    tableHeader.setForeground(TEXT_DARK);
    tableHeader.setReorderingAllowed(false);
    tableHeader.setPreferredSize(new Dimension(100, 36));

    JScrollPane tableScroll = new JScrollPane(logTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.getViewport().setBackground(WHITE);
    body.add(tableScroll, "grow, push");

    return body;
  }

  /** Re-reads the logs with the current filter values (called on construction and on Apply). */
  public void loadLogs() {
    String action = actionFilterActionValue();
    String username = usernameFilter.getText().trim();

    List<SecurityLog> logs;
    try {
      logs = dao.findByFilter(action, username.isEmpty() ? null : username);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    tableModel.setRowCount(0);
    for (SecurityLog log : logs) {
      tableModel.addRow(
          new Object[] {
            displayAction(log),
            log.getUsername() == null ? "" : log.getUsername(),
            log.getRole() == null || log.getRole().isEmpty() ? "--" : log.getRole(),
            displayResult(log),
            log.getHost() == null || log.getHost().isEmpty() ? "--" : log.getHost(),
            log.getCreatedAt() == null ? "--" : log.getCreatedAt()
          });
    }
  }

  private String actionFilterActionValue() {
    switch (actionFilter.getSelectedItem() == null ? "ALL" : actionFilter.getSelectedItem().toString()) {
      case "LOGIN SUCCESS":
        return SecurityLog.LOGIN_SUCCESS;
      case "LOGIN FAIL":
        return SecurityLog.LOGIN_FAIL;
      case "LOGOUT":
        return SecurityLog.LOGOUT;
      default:
        return null; // ALL
    }
  }

  private static String displayAction(SecurityLog log) {
    return switch (log.getAction() == null ? "" : log.getAction()) {
      case SecurityLog.LOGIN_SUCCESS -> "LOGIN";
      case SecurityLog.LOGIN_FAIL -> "LOGIN";
      case SecurityLog.LOGOUT -> "LOGOUT";
      default -> log.getAction();
    };
  }

  private static String displayResult(SecurityLog log) {
    return switch (log.getAction() == null ? "" : log.getAction()) {
      case SecurityLog.LOGIN_SUCCESS -> "Success";
      case SecurityLog.LOGIN_FAIL -> "Failed";
      case SecurityLog.LOGOUT -> "Logged out";
      default -> log.isSuccess() ? "Success" : "Failed";
    };
  }

  // --- Accessors (used by the smoke harness) ---

  /** The table, so a harness can inspect row contents. */
  public JTable getLogTable() {
    return logTable;
  }

  public JComboBox<String> getActionFilter() {
    return actionFilter;
  }

  public JTextField getUsernameFilter() {
    return usernameFilter;
  }

  /** The Apply button. */
  public JButton getApplyButton() {
    return applyButton;
  }

  /** True when the header's "< Back" link (ADMIN_DASHBOARD) is present. */
  public boolean hasBackButton() {
    for (Component c : getComponents()) {
      if (c instanceof JPanel p && findBackLabel(p) != null) {
        return true;
      }
    }
    return false;
  }

  private static JLabel findBackLabel(Container c) {
    for (Component child : c.getComponents()) {
      if (child instanceof JLabel l && "<  Back".equals(l.getText())) {
        return l;
      }
      if (child instanceof Container cont) {
        JLabel found = findBackLabel(cont);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
