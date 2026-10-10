package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.hospitalqueing.database.DatabaseConnection;

/**
 * Stat cards for the admin overview: TOTAL PATIENTS / ACTIVE USERS / UPTIME.
 *
 * <p>Each count is a raw JDBC query wrapped in try/catch, returning {@code -1} on any error
 * (the incidents table may not exist yet — that's fine). The card renders "—" for -1.
 * Uptime is now minus a start time stamped once when this class is first loaded
 * (close enough for a demo).
 */
public class AdminOverviewCards extends JPanel {

  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color TEXT_MUTED = new Color(113, 128, 150);
  private static final Color WHITE = Color.WHITE;
  private static final Color CARD_BORDER = new Color(225, 230, 235);

  /** App-start timestamp for the uptime card (class-load ≈ app launch). */
  private static final long APP_START_MILLIS = System.currentTimeMillis();

  public AdminOverviewCards() {
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(getOverviewPanel(), BorderLayout.NORTH);
  }

  // ================= RAW JDBC COUNTS =================

  /** Count of live (non-trashed) patients; -1 on any error. */
  public long countPatients() {
    return count("SELECT COUNT(*) FROM patients WHERE deleted_at IS NULL");
  }

  /** Count of active, live (non-trashed) user accounts; -1 on any error. */
  public long countActiveUsers() {
    return count(
        "SELECT COUNT(*) FROM users WHERE is_active = 1 AND deleted_at IS NULL");
  }

  /**
   * Count of open incidents (status OPEN or IN_PROGRESS); -1 on any error — including the
   * incidents table not existing yet, which is expected on this worktree.
   */
  public long countOpenIncidents() {
    return count(
        "SELECT COUNT(*) FROM incidents WHERE status IN ('OPEN', 'IN_PROGRESS')");
  }

  private long count(String sql) {
    try (Connection conn = DatabaseConnection.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql)) {
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (Throwable t) {
      // Missing table / unreachable DB: the card shows "—".
    }
    return -1;
  }

  /** Uptime in human-readable form (e.g. "00h 05m 12s"). */
  public String uptime() {
    long seconds = (System.currentTimeMillis() - APP_START_MILLIS) / 1000L;
    return String.format(
        "%02dh %02dm %02ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
  }

  // ================= CARDS =================

  /**
   * The overview: three stat cards in a row, same card style as AdminDashboardPanel's
   * stat cards (white, 1px border, muted 14 bold title, 36 bold value).
   */
  public JPanel getOverviewPanel() {
    JPanel panel =
        new JPanel(new MigLayout("insets 0, gap 16, fillx", "[grow, fill][grow, fill][grow, fill]", "[]"));
    panel.setOpaque(false);
    panel.add(createStatCard("TOTAL PATIENTS", valueOrDash(countPatients()), "🩻", new Color(230, 244, 255), new Color(21, 101, 192)));
    panel.add(createStatCard("ACTIVE USERS", valueOrDash(countActiveUsers()), "👥", new Color(235, 249, 241), new Color(46, 204, 113)));
    panel.add(createStatCard("UPTIME", uptime(), "⏱", new Color(255, 244, 229), new Color(230, 126, 34)));
    return panel;
  }

  private String valueOrDash(long value) {
    return value < 0 ? "— " : String.valueOf(value);
  }

  /** Card style mirrored from AdminDashboardPanel#createStatCard. */
  private JPanel createStatCard(String title, String value, String icon, Color bgColor, Color iconColor) {
    JPanel card = new JPanel(new MigLayout("insets 20, fillx", "[left]push[right]", "[]10[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLbl.setForeground(TEXT_MUTED);
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    iconLbl.setForeground(iconColor);
    JLabel countLbl = new JLabel(value);
    countLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
    countLbl.setForeground(TEXT_DARK);
    card.add(titleLbl, "cell 0 0");
    card.add(iconLbl, "cell 1 0");
    card.add(countLbl, "cell 0 1, span 2");
    return card;
  }

  // ================= SMOKE-TEST ACCESSORS =================

  /** Read the rendered value label of a card (by its title) — smoke verification hook. */
  public JLabel getCardValue(String title) {
    for (Component c : getOverviewPanel().getComponents()) {
      if (c instanceof JPanel) {
        for (Component cardPart : ((JPanel) c).getComponents()) {
          if (cardPart instanceof JLabel lbl
              && new Font("SansSerif", Font.BOLD, 14).equals(lbl.getFont())
              && title.equals(lbl.getText())) {
            return (JLabel) ((JPanel) c).getComponents()[2];
          }
        }
      }
    }
    return null;
  }
}
