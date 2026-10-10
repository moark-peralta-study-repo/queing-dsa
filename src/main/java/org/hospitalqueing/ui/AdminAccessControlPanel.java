package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.User;

/**
 * Admin section: ACCESS CONTROL (security-flow expansion). Every live user account in one table
 * (Username | Role | Active | Last Login) with a per-row [Revoke]/[Grant] toggle that flips
 * {@code users.is_active} via {@link UserDAO}.
 *
 * <p>"Last Login" is the newest {@code security_logs} row for that username
 * ({@code SELECT MAX(created_at) ...}). The security_logs table is owned by a different slice of
 * the security expansion, so the whole row-build reads it with raw JDBC inside a
 * {@code try/catch(Throwable)}: a missing table (or any other failure) degrades to "—" per row
 * instead of breaking the panel.
 *
 * <p>Guard: an admin cannot revoke the account they are currently logged in with.
 */
public class AdminAccessControlPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color WHITE = Color.WHITE;
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color TEXT_MUTED = new Color(113, 128, 150);
  private static final Color DANGER = new Color(214, 64, 64);

  private final MainFrame parentFrame;
  private final UserDAO userDAO = new UserDAO();

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {"Username", "Role", "Active", "Last Login", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private JTable usersTable;

  /** User ids aligned to table rows, so the per-row toggle buttons know their target. */
  private final List<Integer> rowIds = new java.util.ArrayList<>();

  public AdminAccessControlPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    add(buildHeader(), BorderLayout.NORTH);

    JPanel body = new JPanel(new BorderLayout(0, 10));
    body.setBackground(BACKGROUND_LIGHT);
    body.setBorder(BorderFactory.createEmptyBorder(24, 40, 16, 40));
    body.add(buildTable(), BorderLayout.CENTER);
    body.add(buildFootnote(), BorderLayout.SOUTH);
    add(body, BorderLayout.CENTER);

    load();
  }

  /** Dark header bar: "< Back" + "ACCESS CONTROL" title. */
  private JPanel buildHeader() {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 15 30 15 30, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));

    JLabel titleLabel = new JLabel("ACCESS CONTROL");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(WHITE);
    headerPanel.add(titleLabel);

    return headerPanel;
  }

  /** The users table in a scroll pane that fills the body (stretches to full width). */
  private JComponent buildTable() {
    JPanel scrollArea =
        new JPanel(new MigLayout("insets 0", "[grow, fill]", "[grow, fill]"));
    scrollArea.setBackground(BACKGROUND_LIGHT);

    usersTable = new JTable(tableModel);
    usersTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
    usersTable.setRowHeight(38);
    usersTable.setGridColor(new Color(230, 230, 230));
    usersTable.setSelectionBackground(new Color(227, 242, 253));
    usersTable.setSelectionForeground(TEXT_DARK);

    JTableHeader tableHeader = usersTable.getTableHeader();
    tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
    tableHeader.setBackground(WHITE);
    tableHeader.setForeground(TEXT_DARK);
    tableHeader.setPreferredSize(new Dimension(100, 40));
    tableHeader.setReorderingAllowed(false);

    JScrollPane tableScroll = new JScrollPane(usersTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.getViewport().setBackground(WHITE);
    scrollArea.add(tableScroll);

    return scrollArea;
  }

  private JLabel buildFootnote() {
    JLabel footnote =
        new JLabel(
            "Role and username changes live in the ACCOUNTS section — here you only grant or "
                + "revoke access.");
    footnote.setFont(new Font("SansSerif", Font.PLAIN, 12));
    footnote.setForeground(TEXT_MUTED);
    footnote.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
    return footnote;
  }

  /** Re-reads every live user (called on open and after any toggle). */
  public void load() {
    tableModel.setRowCount(0);
    rowIds.clear();

    List<User> users;
    try {
      users = userDAO.findAll();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    String currentUsername = currentUsername();

    for (User user : users) {
      rowIds.add(user.getUserId());
      String lastLogin = lastLoginFor(user.getUsername());
      tableModel.addRow(
          new Object[] {
            user.getUsername(),
            UiData.roleNameForUser(user),
            user.isActive() ? "Active" : "Inactive",
            lastLogin
          });
      int r = tableModel.getRowCount() - 1;
      JButton toggle = new JButton(user.isActive() ? "Revoke" : "Grant");
      styleSmallButton(toggle, user.isActive() ? DANGER : new Color(46, 125, 50));
      if (!user.isActive() || !user.getUsername().equals(currentUsername)) {
        toggle.addActionListener(e -> confirmToggle(user));
      } else {
        // Self: clicking still shows the guard (the smoke harness expects the message).
        toggle.addActionListener(e -> selfGuard());
      }
      tableModel.setValueAt(toggle, r, 4);
    }
  }

  /**
   * The account's most recent security log timestamp, or "—" when security_logs has no row for it
   * — or on ANY error (the table may not exist yet; another slice of the security work owns it).
   * Whole read is raw JDBC in try/catch(Throwable) so this panel always survives.
   */
  private String lastLoginFor(String username) {
    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT MAX(created_at) FROM security_logs WHERE username = ?")) {
      statement.setString(1, username);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String value = resultSet.getString(1);
          return value == null || value.isBlank() ? "—" : value;
        }
        return "—";
      }
    } catch (Throwable t) {
      return "—";
    }
  }

  private String currentUsername() {
    if (parentFrame != null) {
      User u = parentFrame.getLoggedInUser();
      if (u != null && u.getUsername() != null) {
        return u.getUsername();
      }
    }
    return null;
  }

  /** The logged-in account cannot be revoked from this screen (the admin would lock themselves out). */
  private void selfGuard() {
    JOptionPane.showMessageDialog(
        this,
        "You cannot revoke the account you are currently logged in with.",
        "Access Control",
        JOptionPane.WARNING_MESSAGE);
  }

  private void confirmToggle(User user) {
    boolean activating = !user.isActive();
    String message =
        activating
            ? "Grant access to \"" + user.getUsername() + "\"?"
            : "Revoke access from \"" + user.getUsername() + "\"?\nThey will not be able to log in until re-granted.";
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            message,
            activating ? "Grant Access" : "Revoke Access",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      User fresh = userDAO.findById(user.getUserId());
      fresh.setActive(activating);
      userDAO.update(fresh);
      load();
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
          this,
          "Could not update account: " + ex.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
      load();
    }
  }

  private void styleSmallButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 11));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  // ================= SMOKE-TEST ACCESSORS =================

  public JTable getTable() {
    return usersTable;
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }

  /** The [Revoke]/[Grant] button for a table row (column 4), or null. */
  public JButton getToggleButtonAt(int row) {
    Object cell = tableModel.getValueAt(row, 4);
    return cell instanceof JButton ? (JButton) cell : null;
  }

  /** The user id backing a table row (aligned list), or -1. */
  public int getRowIdAt(int row) {
    return row >= 0 && row < rowIds.size() ? rowIds.get(row) : -1;
  }

  /** Finds the table row index for the user with the given username, or -1. */
  public int findRowByUsername(String username) {
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      Object cell = tableModel.getValueAt(i, 0);
      if (cell != null && cell.toString().equals(username)) {
        return i;
      }
    }
    return -1;
  }

  /** The "Last Login" cell text for a table row, or null. */
  public String getLastLoginAt(int row) {
    Object cell = tableModel.getValueAt(row, 3);
    return cell == null ? null : cell.toString();
  }
}
