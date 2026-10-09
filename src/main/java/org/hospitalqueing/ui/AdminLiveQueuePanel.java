package org.hospitalqueing.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * Admin "LIVE QUEUE MONITOR" (TASK C): every department in one screen, each rendered as a
 * mini waiting-room card — a Now-Serving label plus that department's live active queue.
 *
 * <p>The Now-Serving number per card reuses the WaitingRoomPanel rule: the highest non-terminal
 * queue number currently at a counter ({@code Checked In} / {@code In Consultation}) for that
 * department, falling back to the head of the active queue when nothing is being served
 * ({@link QueueManagementService#getActiveQueue} already returns it in serving order).
 *
 * <p>Like {@link WaitingRoomPanel}, a Swing {@link javax.swing.Timer} (3000 ms) re-queries ALL
 * department cards on the EDT — a ticket joined on a staff laptop (or by the phone app) shows up
 * here without anyone touching the screen.
 */
public class AdminLiveQueuePanel extends JPanel {

  private final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final QueueManagementService qms =
      new QueueManagementService(
          new QueueEntryDAO(), new QueueEventDAO(), new ServiceDAO(), new DepartmentDAO(), new CounterDAO());

  /** Cards in stable department-id order (a card is (re)built when the dept set changes). */
  private final Map<Integer, DepartmentCard> cards = new LinkedHashMap<>();
  private JPanel gridPanel;
  private javax.swing.Timer liveTimer;

  /** One department's mini waiting-room card: Now-Serving label + live active-queue table. */
  public static class DepartmentCard extends JPanel {
    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color TEXT_MUTED = new Color(113, 128, 150);

    private final Department department;
    private final JLabel nowServingValue;
    private final JLabel nowServingSubtitle;
    private final JTable queueTable;
    private final DefaultTableModel queueTableModel =
        new DefaultTableModel(new Object[] {"Ticket", "Patient", "Status"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    DepartmentCard(Department department) {
      this.department = department;
      setLayout(new BorderLayout());
      setBackground(WHITE);
      setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
      setOpaque(true);

      // Card title: the department name.
      JLabel deptTitle = new JLabel(department.getDepartmentName());
      deptTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
      deptTitle.setForeground(TEXT_DARK);
      deptTitle.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
      add(deptTitle, BorderLayout.NORTH);

      // Now-Serving row (label + big ticket, same data the waiting room shows).
      JPanel nowRow = new JPanel(new BorderLayout(10, 0));
      nowRow.setBackground(WHITE);
      nowRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
      JLabel nowLabel = new JLabel("NOW SERVING");
      nowLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
      nowLabel.setForeground(TEXT_MUTED);
      nowServingValue = new JLabel("—");
      nowServingValue.setFont(new Font("SansSerif", Font.BOLD, 20));
      nowServingValue.setForeground(PRIMARY_BLUE);
      nowServingSubtitle = new JLabel("");
      nowServingSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
      nowServingSubtitle.setForeground(TEXT_MUTED);
      JPanel nowTexts = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      nowTexts.setOpaque(false);
      nowTexts.add(nowLabel);
      nowRow.add(nowTexts, BorderLayout.WEST);
      nowRow.add(nowServingValue, BorderLayout.CENTER);
      add(nowRow, BorderLayout.NORTH);

      // Active queue table (Ticket | Patient | Status) — compact, scrollable.
      queueTable = new JTable(queueTableModel);
      queueTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
      queueTable.setRowHeight(28);
      queueTable.setGridColor(new Color(235, 235, 235));
      queueTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
      queueTable.getTableHeader().setReorderingAllowed(false);
      JScrollPane scroll = new JScrollPane(queueTable);
      scroll.setBorder(null);
      scroll.getViewport().setBackground(WHITE);
      scroll.setPreferredSize(new Dimension(340, 92));
      scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 148));
      add(scroll, BorderLayout.CENTER);
    }

    /** Re-reads this department's live queue and repaints the card (same rule as WaitingRoomPanel). */
    void refresh(QueueManagementService qms) {
      List<QueueEntry> active;
      try {
        active = qms.getActiveQueue(department.getDepartmentId());
      } catch (Exception ex) {
        // A failed read shouldn't blank the whole monitor.
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
        nowServingValue.setText(UiData.queueLabel(department.getDepartmentName(), servingNumber));
        nowServingSubtitle.setText("At the counter");
      } else if (head != null) {
        nowServingValue.setText(UiData.queueLabel(department.getDepartmentName(), head.getQueueNumber()));
        nowServingSubtitle.setText("Next in line");
      } else {
        nowServingValue.setText("—");
        nowServingSubtitle.setText("No active queue");
      }

      queueTableModel.setRowCount(0);
      for (QueueEntry e : active) {
        queueTableModel.addRow(
            new Object[] {
              UiData.queueLabel(department.getDepartmentName(), e.getQueueNumber()),
              UiData.patientName(e.getPatientId()),
              e.getStatus()
            });
      }
      revalidate();
      repaint();
    }

    // --- Accessors (used by the smoke harness) ---
    public String getDepartmentName() {
      return department.getDepartmentName();
    }

    public int getDepartmentId() {
      return department.getDepartmentId();
    }

    public String getNowServingText() {
      return nowServingValue.getText();
    }

    public String getNowServingSubtitle() {
      return nowServingSubtitle.getText();
    }

    public int getActiveRowCount() {
      return queueTableModel.getRowCount();
    }

    public String getTicketAt(int row) {
      return queueTableModel.getValueAt(row, 0) == null ? null : queueTableModel.getValueAt(row, 0).toString();
    }

    public String getStatusAt(int row) {
      return queueTableModel.getValueAt(row, 2) == null ? null : queueTableModel.getValueAt(row, 2).toString();
    }

    public JTable getQueueTable() {
      return queueTable;
    }
  }

  public AdminLiveQueuePanel(MainFrame parentFrame) {
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    // --- 1. DARK HEADER: back link + title + live badge ---
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
    left.setOpaque(false);
    left.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));
    JLabel titleLabel = new JLabel("LIVE QUEUE MONITOR");
    titleLabel.setForeground(WHITE);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    left.add(titleLabel);

    JLabel liveBadge = new JLabel("● LIVE · auto-refresh 3s");
    liveBadge.setForeground(new Color(126, 224, 156));
    liveBadge.setFont(new Font("SansSerif", Font.PLAIN, 12));

    headerPanel.add(left, BorderLayout.WEST);
    headerPanel.add(liveBadge, BorderLayout.EAST);
    add(headerPanel, BorderLayout.NORTH);

    // --- 2. BODY: one card per department, scrollable 2-column grid ---
    gridPanel = new JPanel(new GridBagLayout());
    gridPanel.setBackground(BACKGROUND_LIGHT);
    gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    gridPanel.setOpaque(true);

    JScrollPane scroll = new JScrollPane(gridPanel);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(BACKGROUND_LIGHT);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll, BorderLayout.CENTER);

    // Start the live-refresh timer: every 3s re-read EVERY department card (same cadence and
    // mechanism as WaitingRoomPanel), so joins from staff laptops / the phone appear on their own.
    liveTimer = new javax.swing.Timer(3000, e -> refresh());
    liveTimer.start();

    refresh();
  }

  /**
   * Re-queries everything: the department list (cards are (re)built only when the set of
   * departments changes), then each card's Now-Serving + active queue. Called from the timer and
   * from the parent whenever the screen is shown.
   */
  public void refresh() {
    if (gridPanel == null) return;

    // 1. Ensure a card exists for EVERY department (rebuild the grid only when the set changes).
    List<Department> departments;
    try {
      departments = new DepartmentDAO().findAll();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    boolean sameSet = departments.size() == cards.size();
    if (sameSet) {
      for (Department d : departments) {
        if (!cards.containsKey(d.getDepartmentId())) {
          sameSet = false;
          break;
        }
      }
    }
    if (!sameSet) {
      cards.clear();
      for (Department d : departments) {
        cards.put(d.getDepartmentId(), new DepartmentCard(d));
      }
      rebuildGrid();
    }

    // 2. Re-read live data into every existing card.
    for (DepartmentCard card : cards.values()) {
      card.refresh(qms);
    }
  }

  private void rebuildGrid() {
    gridPanel.removeAll();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 14, 14, 14);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    int row = 0;
    for (DepartmentCard card : cards.values()) {
      gbc.gridx = row % 2;
      gbc.gridy = row / 2;
      gridPanel.add(card, gbc);
      row++;
    }
    gridPanel.revalidate();
    gridPanel.repaint();
  }

  // --- Accessors (used by the smoke harness and any future driver) ---

  /** All department cards, in the panel's display (department-id) order. */
  public List<DepartmentCard> getDepartmentCards() {
    return List.copyOf(cards.values());
  }

  public DepartmentCard getCardForDepartment(String departmentName) {
    for (DepartmentCard card : cards.values()) {
      if (card.getDepartmentName().equalsIgnoreCase(departmentName)) {
        return card;
      }
    }
    return null;
  }

  /** True when the header's "< Back" link (ADMIN_DASHBOARD) is present. */
  public boolean hasBackButton() {
    for (Component c : getComponents()) {
      if (c instanceof JPanel header && findBackLabel(header) != null) {
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

  public void stopTimer() {
    if (liveTimer != null) {
      liveTimer.stop();
    }
  }
}
