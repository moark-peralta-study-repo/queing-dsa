package org.hospitalqueing.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
 * Waiting-room Now-Serving display (T4).
 *
 * <p>A large-format screen meant for a lobby monitor: a huge "Now Serving" ticket label for the
 * selected department (department-prefixed via {@link UiData#queueLabel}), with the department's
 * live active queue listed below. The Now-Serving number is the highest non-terminal queue number
 * currently at a counter ({@code Checked In} / {@code In Consultation}) for that department; when
 * nothing is being served it falls back to the head of the active queue (which
 * {@link QueueManagementService#getActiveQueue} already returns in serving order). A department
 * combo box switches the displayed department. All data is read live from the database, and
 * {@link #refresh()} re-reads it whenever the screen is shown or the department changes.
 *
 * <p>Style follows the dashboard shell: dark navy header, white cards on a light background.
 */
public class WaitingRoomPanel extends JPanel {

  private final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final QueueManagementService qms =
      new QueueManagementService(
          new QueueEntryDAO(), new QueueEventDAO(), new ServiceDAO(), new DepartmentDAO(), new CounterDAO());

  private JComboBox<Department> departmentCombo;
  private JLabel nowServingValue;
  private JLabel nowServingSubtitle;
  private JLabel queueCardTitle;
  private JTable queueTable;
  private final DefaultTableModel queueTableModel =
      new DefaultTableModel(new Object[] {"Ticket", "Patient", "Joined", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

  public WaitingRoomPanel() {
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    // --- 1. DARK HEADER with the department switch ---
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));

    JLabel logoLabel = new JLabel("✚");
    logoLabel.setForeground(WHITE);
    logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

    JLabel titleLabel = new JLabel("WAITING ROOM");
    titleLabel.setForeground(WHITE);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    left.setOpaque(false);
    left.add(logoLabel);
    left.add(titleLabel);

    JLabel deptPrompt = new JLabel("Department:");
    deptPrompt.setForeground(new Color(180, 195, 215));
    deptPrompt.setFont(new Font("SansSerif", Font.PLAIN, 13));

    departmentCombo = new JComboBox<>();
    departmentCombo.setRenderer(
        new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(value instanceof Department d ? d.getDepartmentName() : "No departments");
            return this;
          }
        });
    departmentCombo.setBackground(WHITE);
    departmentCombo.setFont(new Font("SansSerif", Font.BOLD, 14));
    departmentCombo.setPreferredSize(new Dimension(220, 36));
    for (Department d : new DepartmentDAO().findAll()) {
      departmentCombo.addItem(d);
    }
    departmentCombo.addActionListener(e -> refresh());

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    right.setOpaque(false);
    right.add(deptPrompt);
    right.add(departmentCombo);

    headerPanel.add(left, BorderLayout.WEST);
    headerPanel.add(right, BorderLayout.EAST);
    add(headerPanel, BorderLayout.NORTH);

    // --- 2. NOW-SERVING CARD (white, huge ticket label) ---
    JPanel nowServingCard = new JPanel(new GridBagLayout());
    nowServingCard.setBackground(WHITE);
    nowServingCard.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel nowServingPrompt = new JLabel("NOW SERVING");
    nowServingPrompt.setFont(new Font("SansSerif", Font.BOLD, 16));
    nowServingPrompt.setForeground(TEXT_MUTED);
    gbc.insets = new Insets(28, 0, 4, 0);
    nowServingCard.add(nowServingPrompt, gbc);

    nowServingValue = new JLabel("—");
    nowServingValue.setFont(new Font("SansSerif", Font.BOLD, 92));
    nowServingValue.setForeground(PRIMARY_BLUE);
    nowServingValue.setHorizontalAlignment(SwingConstants.CENTER);
    nowServingCard.add(nowServingValue, gbc);

    nowServingSubtitle = new JLabel("");
    nowServingSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
    nowServingSubtitle.setForeground(TEXT_DARK);
    nowServingSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
    gbc.insets = new Insets(0, 0, 24, 0);
    nowServingCard.add(nowServingSubtitle, gbc);

    // --- 3. ACTIVE QUEUE CARD (live rows for the selected department) ---
    JPanel queueCard = new JPanel(new BorderLayout());
    queueCard.setBackground(WHITE);
    queueCard.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

    queueCardTitle = new JLabel("Active Queue");
    queueCardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
    queueCardTitle.setForeground(TEXT_DARK);
    queueCardTitle.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));
    queueCard.add(queueCardTitle, BorderLayout.NORTH);

    queueTable = new JTable(queueTableModel);
    queueTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
    queueTable.setRowHeight(32);
    queueTable.setGridColor(new Color(230, 230, 230));
    queueTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
    queueTable.getTableHeader().setReorderingAllowed(false);
    JScrollPane queueScroll = new JScrollPane(queueTable);
    queueScroll.setBorder(null);
    queueScroll.getViewport().setBackground(WHITE);
    queueCard.add(queueScroll, BorderLayout.CENTER);

    JPanel center = new JPanel(new BorderLayout(0, 18));
    center.setBackground(BACKGROUND_LIGHT);
    center.setBorder(BorderFactory.createEmptyBorder(22, 36, 22, 36));
    center.add(nowServingCard, BorderLayout.NORTH);
    center.add(queueCard, BorderLayout.CENTER);
    add(center, BorderLayout.CENTER);

    refresh();
  }

  /** Re-reads the selected department's queue and repaints the display. */
  public void refresh() {
    if (departmentCombo == null) return;
    Department dept = (Department) departmentCombo.getSelectedItem();
    if (dept == null) {
      nowServingValue.setText("—");
      nowServingSubtitle.setText("No departments");
      queueCardTitle.setText("Active Queue");
      queueTableModel.setRowCount(0);
      return;
    }

    List<QueueEntry> active;
    try {
      active = qms.getActiveQueue(dept.getDepartmentId());
    } catch (Exception ex) {
      // A failed read shouldn't blank the display.
      ex.printStackTrace();
      active = List.of();
    }

    // Now Serving: highest non-terminal number currently at a counter
    // (Checked In / In Consultation); otherwise the head of the active queue.
    int servingNumber = -1;
    for (QueueEntry e : active) {
      if (QueueStatus.isAtCounter(e.getStatus()) && e.getQueueNumber() > servingNumber) {
        servingNumber = e.getQueueNumber();
      }
    }
    QueueEntry head = active.isEmpty() ? null : active.get(0);

    if (servingNumber > 0) {
      nowServingValue.setText(UiData.queueLabel(dept.getDepartmentName(), servingNumber));
      nowServingSubtitle.setText(dept.getDepartmentName() + " · At the counter");
    } else if (head != null) {
      nowServingValue.setText(UiData.queueLabel(dept.getDepartmentName(), head.getQueueNumber()));
      nowServingSubtitle.setText(dept.getDepartmentName() + " · Next in line");
    } else {
      nowServingValue.setText("—");
      nowServingSubtitle.setText(dept.getDepartmentName() + " · No active queue");
    }

    queueCardTitle.setText("Active Queue — " + dept.getDepartmentName());
    queueTableModel.setRowCount(0);
    for (QueueEntry e : active) {
      queueTableModel.addRow(
          new Object[] {
            UiData.queueLabel(dept.getDepartmentName(), e.getQueueNumber()),
            UiData.patientName(e.getPatientId()),
            formatJoinedAt(e.getJoinedAt()),
            e.getStatus()
          });
    }
    if (queueTable != null && queueTable.getParent() != null) {
      queueTable.getParent().validate();
    }
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

  // --- Accessors (used by the smoke harness and any future driver of the display) ---

  public String getNowServingText() {
    return nowServingValue.getText();
  }

  public int getActiveRowCount() {
    return queueTableModel.getRowCount();
  }

  public JComboBox<Department> getDepartmentCombo() {
    return departmentCombo;
  }
}
