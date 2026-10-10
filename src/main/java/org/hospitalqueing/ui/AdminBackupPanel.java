package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.hospitalqueing.dao.BackupDAO;
import org.hospitalqueing.service.BackupService;

/**
 * Admin section (system backup): the live database status card (path, size, uptime, last backup)
 * plus a full-width [BACK UP NOW] action, and the backup history table with a per-row [RESTORE].
 *
 * <p>Backups are timestamped copies of {@code hospital.db} under {@code backups/}, recorded in
 * {@code backup_history} (see {@link BackupService}). A restore copies the newest backup back over
 * the live DB; the UI then advises a restart so in-memory caches reload.
 */
public class AdminBackupPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color CARD_BORDER = new Color(225, 230, 235);
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color TEXT_MUTED = new Color(113, 128, 150);

  private static final DateTimeFormatter CREATED_VIEW_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /** App start ≈ when this panel class first loads (built by MainFrame right at boot). */
  private static final long APP_START_MILLIS = System.currentTimeMillis();

  private final MainFrame parentFrame;
  private final BackupService backupService = new BackupService();
  private final BackupDAO backupDAO = new BackupDAO();

  // Left card value labels (re-filled by refresh()).
  private final JLabel dbPathLabel = new JLabel("—");
  private final JLabel dbSizeLabel = new JLabel("—");
  private final JLabel uptimeLabel = new JLabel("—");
  private final JLabel lastBackupLabel = new JLabel("—");
  private final JButton backupNowButton;

  // Right card table.
  private final DefaultTableModel backupsModel =
      new DefaultTableModel(new Object[]{"File", "Size", "Created", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false; // button column (Restore): no text editor on double-click
        }
      };
  private JTable backupsTable;

  public AdminBackupPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;

    backupNowButton = new JButton("BACK UP NOW");
    styleButton(backupNowButton, PRIMARY_BLUE);
    backupNowButton.addActionListener(e -> onBackupNow());

    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildHeader(), BorderLayout.NORTH);
    add(buildBody(), BorderLayout.CENTER);

    refresh();
  }

  /** Dark header bar: "&lt; Back" to the admin dashboard (left) + the "SYSTEM BACKUP" title. */
  private JPanel buildHeader() {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 12 20 12 20, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));
    JLabel titleLabel = new JLabel("SYSTEM BACKUP");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(Color.WHITE);
    headerPanel.add(titleLabel);
    return headerPanel;
  }

  /** Two cards side-by-side (each grows to half the width) on the light body background. */
  private JPanel buildBody() {
    JPanel body = new JPanel(new MigLayout("insets 15 30 15 30, gapx 20", "[grow 1, fill][grow 1, fill]", "[grow, fill]"));
    body.setOpaque(false);
    body.add(buildDatabaseCard(), "cell 0 0, growy");
    body.add(buildBackupsCard(), "cell 1 0, growy");
    return body;
  }

  // ================= LEFT CARD: DATABASE =================

  private JPanel buildDatabaseCard() {
    JPanel card = new JPanel(new MigLayout("insets 20, fillx, gapy 8", "[left, grow 0][grow, fill]", "[]14[]"));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));

    card.add(cardTitle("DATABASE"), "span 2, gaptop 0");

    card.add(rowLabel("Database file"));
    dbPathLabel.setForeground(TEXT_MUTED);
    card.add(dbPathLabel);

    card.add(rowLabel("Size"));
    dbSizeLabel.setForeground(TEXT_DARK);
    card.add(dbSizeLabel);

    card.add(rowLabel("Uptime"));
    uptimeLabel.setForeground(TEXT_DARK);
    card.add(uptimeLabel);

    card.add(rowLabel("Last backup"));
    lastBackupLabel.setForeground(TEXT_DARK);
    card.add(lastBackupLabel);

    card.add(backupNowButton, "span 2, gaptop 10");
    return card;
  }

  private JLabel cardTitle(String text) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("SansSerif", Font.BOLD, 14));
    l.setForeground(PRIMARY_BLUE);
    return l;
  }

  private JLabel rowLabel(String text) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("SansSerif", Font.PLAIN, 13));
    l.setForeground(TEXT_MUTED);
    return l;
  }

  // ================= RIGHT CARD: BACKUPS =================

  private JPanel buildBackupsCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));

    card.add(cardTitle("BACKUPS"), BorderLayout.NORTH);

    backupsTable = new JTable(backupsModel);
    backupsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
    backupsTable.setBackground(Color.WHITE);
    backupsTable.setRowHeight(34);
    TableButtons.renderButtons(backupsTable, 3);
    backupsTable.setIntercellSpacing(new Dimension(4, 4));
    backupsTable.setGridColor(new Color(238, 240, 244));
    backupsTable.setSelectionBackground(new Color(227, 242, 253));
    backupsTable.setSelectionForeground(TEXT_DARK);
    backupsTable.getTableHeader().setReorderingAllowed(false);
    backupsTable.setFillsViewportHeight(true);

    JTableHeader header = backupsTable.getTableHeader();
    header.setFont(new Font("SansSerif", Font.BOLD, 12));
    header.setForeground(TEXT_DARK);
    header.setPreferredSize(new Dimension(100, 36));

    JScrollPane scroll = new JScrollPane(backupsTable);
    scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    card.add(scroll, BorderLayout.CENTER);
    return card;
  }

  // ================= DATA =================

  /** Re-reads the DB stats and the backup history. Called on construction and after any action. */
  public void refresh() {
    // Left card: live DB path + size.
    Path dbPath = Paths.get(BackupService.DB_FILE);
    if (Files.exists(dbPath)) {
      dbPathLabel.setText(dbPath.toAbsolutePath().toString());
      try {
        dbSizeLabel.setText(BackupService.humanSize(Files.size(dbPath)));
      } catch (Exception ex) {
        dbSizeLabel.setText("—");
      }
    } else {
      dbPathLabel.setText(dbPath.toString() + " (not found)");
      dbSizeLabel.setText("—");
    }

    // Left card: uptime since app start.
    long ms = System.currentTimeMillis() - APP_START_MILLIS;
    long totalMinutes = ms / 60000;
    uptimeLabel.setText((totalMinutes / 60) + "h " + (totalMinutes % 60) + "m");

    // Left card: last backup (or "—").
    try {
      BackupDAO.BackupEntry latest = backupDAO.latest();
      lastBackupLabel.setText(latest == null ? "—" : latest.filePath() + "  ·  " + formatCreated(latest.createdAt()));
    } catch (Exception ex) {
      lastBackupLabel.setText("—");
    }

    // Right card: history, newest first.
    backupsModel.setRowCount(0);
    List<BackupDAO.BackupEntry> entries;
    try {
      entries = backupDAO.list();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    for (BackupDAO.BackupEntry entry : entries) {
      backupsModel.addRow(
          new Object[] {
            entry.filePath(),
            BackupService.humanSize(entry.fileSize()),
            formatCreated(entry.createdAt())
          });
      int r = backupsModel.getRowCount() - 1;
      JButton restore = new JButton("RESTORE");
      styleSmallButton(restore, PRIMARY_BLUE);
      restore.addActionListener(e -> onRestore(entry));
      backupsModel.setValueAt(restore, r, 3);
    }
  }

  private static String formatCreated(String iso) {
    if (iso == null || iso.isBlank()) {
      return "—";
    }
    try {
      return LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(CREATED_VIEW_FMT);
    } catch (Exception ex) {
      return iso;
    }
  }

  // ================= ACTIONS =================

  /** [BACK UP NOW]: snapshot the live DB, confirm with a success dialog, refresh both cards. */
  private void onBackupNow() {
    try {
      String path = backupService.createBackup();
      long size = Files.size(Paths.get(path));
      JOptionPane.showMessageDialog(
          this,
          "Backup created successfully.\n\n" + path + "\nSize: " + BackupService.humanSize(size),
          "Backup complete",
          JOptionPane.INFORMATION_MESSAGE);
      refresh();
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "Could not create backup: " + ex.getMessage(), "Backup failed", JOptionPane.ERROR_MESSAGE);
    }
  }

  /** Per-row [RESTORE]: confirm, copy the newest backup over hospital.db, advise a restart. */
  private void onRestore(BackupDAO.BackupEntry entry) {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Restore the database from this backup?\n\n"
                + entry.filePath() + "  (" + formatCreated(entry.createdAt()) + ")\n\n"
                + "The current hospital.db will be replaced.",
            "Restore backup",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      String source = backupService.restoreLatest();
      JOptionPane.showMessageDialog(
          this,
          "Database restored from " + source + ".\n\nRestart the app to be sure all caches reload.",
          "Restore complete",
          JOptionPane.INFORMATION_MESSAGE);
      refresh();
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "Could not restore backup: " + ex.getMessage(), "Restore failed", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ================= STYLING + SMOKE-TEST ACCESSORS =================

  private void styleButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 13));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setPreferredSize(new Dimension(160, 38));
  }

  private void styleSmallButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 11));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public JTable getBackupsTable() {
    return backupsTable;
  }

  public DefaultTableModel getBackupsModel() {
    return backupsModel;
  }

  public JButton getBackupNowButton() {
    return backupNowButton;
  }

  public JLabel getLastBackupLabel() {
    return lastBackupLabel;
  }
}
