package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.hospitalqueing.database.DatabaseConnection;

/**
 * Admin section: system settings. CCTV feed URLs for the CCTV monitor panel, the backup
 * directory (informational), and maintenance mode (persisted; behavior is planned, not wired yet).
 *
 * <p>All persistence is raw JDBC upserts into {@code system_settings(key, value)} — this panel
 * deliberately never imports SettingsDAO (another workstream owns it and it may land after this
 * one). Missing table / DB errors surface as a friendly dialog instead of a stack trace.
 */
public class AdminSystemSettingsPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color MUTED = new Color(113, 128, 150);

  /** Setting keys stored in system_settings. */
  static final String[] KEYS = {
    "cctv_url_1", "cctv_url_2", "cctv_url_3", "cctv_url_4", "backup_dir", "maintenance_mode"
  };

  private final MainFrame parentFrame;
  private final JPanel form;
  private JButton saveButton;

  public AdminSystemSettingsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildHeader(), BorderLayout.NORTH);
    form = buildForm();
    add(form, BorderLayout.CENTER);
  }

  /** Dark header bar: "&lt; Back" to the admin dashboard (left) + "SYSTEM SETTINGS" title. */
  private JPanel buildHeader() {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 12 20 12 20, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(getBackButton());
    JLabel titleLabel = new JLabel("SYSTEM SETTINGS");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    titleLabel.setForeground(Color.WHITE);
    headerPanel.add(titleLabel);
    return headerPanel;
  }

  /** "<  Back" label (admin convention) — smoke hook to verify navigation wiring. */
  public JLabel getBackButton() {
    return BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD");
  }

  private JPanel buildForm() {
    JPanel form =
        new JPanel(
            new MigLayout(
                "insets 30 40 30 40, wrap, gap 10, fillx", "[right][grow, fill]"));
    form.setOpaque(false);

    form.add(new JLabel(""));
    form.add(sectionHeading("CCTV Feeds"));

    form.add(new JLabel("CAM 01 — Front:"));
    form.add(makeTextField("cctv_url_1", "e.g. http://cam1/front.m3u8"));
    form.add(new JLabel("CAM 02 — Lobby:"));
    form.add(makeTextField("cctv_url_2", "e.g. http://cam2/lobby.m3u8"));
    form.add(new JLabel("CAM 03 — ER:"));
    form.add(makeTextField("cctv_url_3", "e.g. http://cam3/er.m3u8"));
    form.add(new JLabel("CAM 04 — Pharmacy:"));
    form.add(makeTextField("cctv_url_4", "e.g. http://cam4/pharmacy.m3u8"));

    JPanel gap = new JPanel();
    gap.setOpaque(false);
    gap.setPreferredSize(new Dimension(0, 6));
    form.add(gap);
    form.add(gap);

    form.add(new JLabel("Maintenance Mode:"));
    form.add(makeCombo("maintenance_mode", new String[] {"OFF", "ON"}));
    form.add(hintLabel("Planned — persisted, no behavior wired yet."));

    form.add(new JLabel("Backup Directory:"));
    JTextField backupField = makeTextFieldDefault("backup_dir", "backups", "Informational — default \"backups\"");
    backupField.setEditable(false);
    backupField.setOpaque(true);
    backupField.setBackground(new Color(240, 243, 247));
    form.add(backupField);

    saveButton = new JButton("SAVE");
    saveButton.setFocusPainted(false);
    saveButton.setBorderPainted(false);
    saveButton.setBackground(new Color(21, 101, 192));
    saveButton.setForeground(Color.WHITE);
    saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
    saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    saveButton.setPreferredSize(new Dimension(160, 36));
    saveButton.addActionListener(e -> onSaveClicked());
    form.add(new JLabel(""));
    form.add(saveButton, "gaptop 10");

    return form;
  }

  private JTextField makeTextField(String key, String placeholder) {
    return makeTextFieldDefault(key, "", placeholder);
  }

  private JTextField makeTextFieldDefault(String key, String defaultValue, String placeholder) {
    JTextField field = new JTextField(loadSetting(key, defaultValue), 28);
    field.putClientProperty("settings.key", key);
    field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    field.setToolTipText(placeholder);
    return field;
  }

  private JComboBox<String> makeCombo(String key, String[] options) {
    JComboBox<String> combo = new JComboBox<>(options);
    String stored = loadSetting(key, options[0]);
    int index = 0;
    for (int i = 0; i < combo.getItemCount(); i++) {
      if (String.valueOf(combo.getItemAt(i)).equals(stored)) {
        index = i;
        break;
      }
    }
    combo.setSelectedIndex(index);
    combo.putClientProperty("settings.key", key);
    combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    return combo;
  }

  private JLabel sectionHeading(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 15));
    label.setForeground(TEXT_DARK);
    return label;
  }

  private JLabel hintLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.PLAIN, 11));
    label.setForeground(MUTED);
    return label;
  }

  private void onSaveClicked() {
    boolean ok = saveAll();
    if (ok) {
      JOptionPane.showMessageDialog(
          this, "Settings saved.", "System Settings", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(
          this,
          "settings table not available yet",
          "System Settings",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Raw-JDBC read of one setting. Returns {@code fallback} when the table is missing, the value
   * is unset, or the DB is unreachable (panel must still build in every state).
   */
  private String loadSetting(String key, String fallback) {
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT value FROM system_settings WHERE key = ?")) {
      ps.setString(1, key);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    } catch (Throwable t) {
      // settings table not available yet / DB closed — fall through to the default.
    }
    return fallback;
  }

  /**
   * Persists every field as a raw-JDBC upsert. Returns false (→ the caller shows the
   * "settings table not available yet" dialog) if the table is missing or any write fails.
   */
  public boolean saveAll() {
    boolean ok = true;
    try (Connection conn = DatabaseConnection.getConnection()) {
      try (PreparedStatement ps =
          conn.prepareStatement(
              "INSERT INTO system_settings(key,value) VALUES(?,?)"
                  + " ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
        for (JComponent field : fields()) {
          ps.setString(1, (String) field.getClientProperty("settings.key"));
          ps.setString(2, field instanceof JComboBox ? String.valueOf(((JComboBox<?>) field).getSelectedItem()) : ((JTextField) field).getText());
          ps.executeUpdate();
        }
      }
    } catch (Throwable t) {
      ok = false;
    }
    return ok;
  }

  private JComponent[] fields() {
    java.util.List<JComponent> list = new java.util.ArrayList<>();
    collectFields(form, list);
    return list.toArray(new JComponent[0]);
  }

  private void collectFields(Container parent, java.util.List<JComponent> out) {
    for (Component c : parent.getComponents()) {
      if (c instanceof JComponent jc) {
        if (jc.getClientProperty("settings.key") != null) {
          out.add(jc);
        }
        if (jc instanceof Container container) {
          collectFields(container, out);
        }
      }
    }
  }

  // ================= SMOKE-TEST ACCESSORS =================

  /** The editable field for a settings key (JTextField or JComboBox). */
  public JComponent getField(String key) {
    for (JComponent field : fields()) {
      if (key.equals(field.getClientProperty("settings.key"))) {
        return field;
      }
    }
    return null;
  }

  public JButton getSaveButton() {
    return saveButton;
  }
}
