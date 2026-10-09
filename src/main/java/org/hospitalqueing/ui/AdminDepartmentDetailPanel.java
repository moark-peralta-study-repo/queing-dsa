package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.controller.DoctorController;
import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueStatus;
import org.hospitalqueing.model.Service;
import org.hospitalqueing.service.DoctorService;
import org.hospitalqueing.service.QueueManagementService;

/**
 * Admin per-DEPARTMENT detail screen (task B): pick a department and see exactly what that
 * department's staff dashboard would show — plus the admin-only doctor roster for it.
 *
 * <ul>
 *   <li>Today's live stats (Today's Patients / In Queue / Completed), dept-scoped the same way
 *       {@link StaffDashboardPanel#refreshHomeStats()} scopes them to a staff member's department.</li>
 *   <li>Queue + appointment history (today's queue entries AND today's dept-scoped appointments,
 *       merged into one Date|Patient|Type|Status list — "like staff sees").</li>
 *   <li>Upcoming appointments (SCHEDULED/CONFIRMED, date &gt;= today, dept-scoped via the service's
 *       department — the same filter {@link DoctorAppointmentsPanel} uses).</li>
 *   <li>Doctors in this department (Name|License|Active) with per-row Edit (name/license/active
 *       dialog) and Remove (blocked "In use" while the doctor has open SCHEDULED/CONFIRMED
 *       appointments, mirroring {@link AdminDoctorsPanel}).</li>
 * </ul>
 *
 * <p>Admin scope ⊇ staff scope: nothing here is invented — every card re-queries the same tables
 * the staff screens read. {@link #refresh()} re-queries all cards; it runs on construction, on
 * department change, and is exposed for the parent/updater.
 */
public class AdminDepartmentDetailPanel extends JPanel {

  private final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final MainFrame parentFrame;
  private final DepartmentDAO departmentDAO = new DepartmentDAO();
  private final QueueEntryDAO queueEntryDAO = new QueueEntryDAO();
  private final AppointmentDAO appointmentDAO = new AppointmentDAO();
  private final ServiceDAO serviceDAO = new ServiceDAO();
  private final DoctorController doctorController =
      new DoctorController(new DoctorService(new DoctorDAO()));
  private final QueueManagementService qms =
      new QueueManagementService(
          queueEntryDAO, new QueueEventDAO(), serviceDAO, departmentDAO, new CounterDAO());

  private JComboBox<String> departmentCombo;

  private final DefaultTableModel historyModel =
      new DefaultTableModel(new Object[]{"Date", "Patient", "Type", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

  private final DefaultTableModel upcomingModel =
      new DefaultTableModel(new Object[]{"Date/Time", "Patient", "Doctor", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

  private final DefaultTableModel doctorsModel =
      new DefaultTableModel(new Object[]{"Name", "License", "Active", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 3;
        }
      };
  private JLabel todayPatientsLabel;
  private JLabel inQueueLabel;
  private JLabel completedLabel;

  public AdminDepartmentDetailPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildHeader(), BorderLayout.NORTH);
    add(buildContent(), BorderLayout.CENTER);
    refresh();
  }

  // --- 1. DARK HEADER: title + back + department picker (top-right) ---

  private JPanel buildHeader() {
    JPanel header =
        new JPanel(
            new MigLayout(
                "insets 15 30 15 30, aligny center",
                "[left]30[left]push[right]",
                "[center]"));
    header.setBackground(HEADER_DARK_BLUE);

    JLabel title = new JLabel("DEPARTMENT DETAIL");
    title.setFont(new Font("SansSerif", Font.BOLD, 16));
    title.setForeground(WHITE);

    // Req: every page needs a "<" back button — route back to the admin dashboard.
    // MainFrame doesn't declare BackButtons.JFrameOwner (owned by the parent task), so adapt
    // it with a lambda: the helper only ever calls showScreen(screenName).
    header.add(title, "cell 0 0");
    header.add(BackButtons.back(name -> parentFrame.showScreen(name), "ADMIN_DASHBOARD"), "cell 1 0");

    // Department picker (top-right): all departments via DepartmentDAO; switching re-queries.
    departmentCombo = new JComboBox<>(UiData.departmentNames());
    departmentCombo.setFont(new Font("SansSerif", Font.BOLD, 12));
    departmentCombo.addActionListener(
        e -> {
          if (e.getSource() == departmentCombo) {
            refresh();
          }
        });
    header.add(departmentCombo, "cell 2 0");

    return header;
  }

  // --- 2. CONTENT: stat cards, history, upcoming, doctors, note ---

  private JPanel buildContent() {
    JPanel scrollArea =
        new JPanel(new MigLayout("insets 24 40 24 40, fillx", "[grow, fill]", "[]16[]"));
    scrollArea.setOpaque(false);

    // 2a. Today's live stats (dept-scoped, staff-dashboard style).
    JPanel stats = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill]", "[]"));
    stats.setOpaque(false);
    todayPatientsLabel = createCountLabel("0");
    inQueueLabel = createCountLabel("0");
    completedLabel = createCountLabel("0");
    stats.add(statCard("Today's Patients", todayPatientsLabel, "👥", new Color(230, 244, 255), PRIMARY_BLUE));
    stats.add(statCard("In Queue", inQueueLabel, "🕒", new Color(255, 244, 229), new Color(230, 126, 34)));
    stats.add(statCard("Completed", completedLabel, "✅", new Color(235, 249, 241), new Color(46, 204, 113)));
    scrollArea.add(stats);

    // 2b. Queue + appointment history (merged sources, dept-scoped).
    scrollArea.add(cardSection("Today's Queue + Appointment History", "Every queue entry and every appointment created today in this department — the merged, dept-scoped history staff sees."));
    JTable historyTable = styledTable(historyModel);
    scrollArea.add(scroll(historyTable));

    // 2c. Upcoming appointments.
    scrollArea.add(cardSection("Upcoming Appointments", "SCHEDULED / CONFIRMED bookings for this department's services, today or later."));
    JTable upcomingTable = styledTable(upcomingModel);
    scrollArea.add(scroll(upcomingTable));

    // 2d. Doctors in this department.
    scrollArea.add(cardSection("Doctors in This Department", "The department's roster with license + active status. Edit updates name/license/active; Remove is blocked while the doctor still has open appointments."));
    JTable doctorsTable = styledTable(doctorsModel);
    doctorsTable.setRowHeight(36);
    scrollArea.add(scroll(doctorsTable));

    // 2e. Scope note.
    JPanel note = new JPanel(new MigLayout("insets 14 20, fillx", "[grow, left]"));
    note.setBackground(new Color(230, 244, 255));
    note.setBorder(BorderFactory.createLineBorder(new Color(166, 206, 240), 1, true));
    JLabel noteLbl = new JLabel("ℹ️  These cards reflect the same live data this department's staff see in the staff dashboard (queue, appointments, roster) — admin scope ⊇ staff scope. Switch the department picker to inspect another unit.");
    noteLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
    noteLbl.setForeground(TEXT_DARK);
    note.add(noteLbl);
    scrollArea.add(note);

    return scrollArea;
  }

  /** Card with a bold title + muted subtitle (the white section header used by the doctor panels). */
  private JPanel cardSection(String title, String subtitle) {
    JPanel card = new JPanel(new MigLayout("insets 20 24, fillx", "[grow, left]", "[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel t = new JLabel(title);
    t.setFont(new Font("SansSerif", Font.BOLD, 16));
    t.setForeground(TEXT_DARK);
    JLabel s = new JLabel(subtitle);
    s.setFont(new Font("SansSerif", Font.PLAIN, 12));
    s.setForeground(TEXT_MUTED);
    card.add(t, "wrap");
    card.add(s, "push");
    return card;
  }

  private JTable styledTable(DefaultTableModel model) {
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowHeight(35);
    return table;
  }

  private JScrollPane scroll(JTable table) {
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    scrollPane.setPreferredSize(new Dimension(700, 220));
    return scrollPane;
  }

  private JPanel statCard(String title, JLabel countLbl, String icon, Color bgColor, Color iconColor) {
    JPanel card = new JPanel(new MigLayout("insets 20, fillx", "[left]push[right]", "[]10[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLbl.setForeground(TEXT_MUTED);
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    iconLbl.setForeground(iconColor);
    card.add(titleLbl, "cell 0 0");
    card.add(iconLbl, "cell 1 0");
    card.add(countLbl, "cell 0 1, span 2");
    return card;
  }

  private JLabel createCountLabel(String count) {
    JLabel countLbl = new JLabel(count);
    countLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
    countLbl.setForeground(TEXT_DARK);
    return countLbl;
  }

  // --- 3. REFRESH (re-query every card for the selected department) ---

  /**
   * Re-queries all cards for the currently selected department. Called on construction, on
   * department change, and exposed for the parent/updater to keep the screen live.
   */
  public void refresh() {
    int deptId = selectedDepartmentId();
    refreshStats(deptId);
    refreshHistory(deptId);
    refreshUpcoming(deptId);
    refreshDoctors(deptId);
  }

  // --- accessors (for the parent updater / smoke tests) ---

  public JComboBox<String> getDepartmentCombo() {
    return departmentCombo;
  }

  /** The "In Queue" live count label (dept-scoped). */
  public JLabel getInQueueCountLabel() {
    return inQueueLabel;
  }

  public DefaultTableModel getHistoryModel() {
    return historyModel;
  }

  public DefaultTableModel getUpcomingModel() {
    return upcomingModel;
  }

  public DefaultTableModel getDoctorsModel() {
    return doctorsModel;
  }

  /** The id of the department currently picked in the combo (or -1 when nothing is selected). */
  private int selectedDepartmentId() {
    String name = (String) departmentCombo.getSelectedItem();
    if (name == null) {
      return -1;
    }
    int id = UiData.departmentIdByName(name);
    if (id > 0) {
      return id;
    }
    for (Department d : departmentDAO.findAll()) {
      if (name.equals(d.getDepartmentName())) {
        return d.getDepartmentId();
      }
    }
    return id;
  }

  // --- 3a. Today's stats — same queries StaffDashboardPanel.refreshHomeStats uses, scoped to deptId ---

  private void refreshStats(int deptId) {
    if (deptId < 0) {
      setCount(todayPatientsLabel, "0");
      setCount(inQueueLabel, "0");
      setCount(completedLabel, "0");
      return;
    }
    String date = LocalDate.now().toString();
    List<QueueEntry> todays =
        queueEntryDAO.findAll().stream()
            .filter(e -> e.getDepartmentId() == deptId)
            .filter(e -> e.getQueueDate() != null && e.getQueueDate().equals(date))
            .toList();
    int completed = 0;
    for (QueueEntry e : todays) {
      if (QueueStatus.isTerminal(e.getStatus())) {
        completed++;
      }
    }
    setCount(todayPatientsLabel, String.valueOf(todays.size()));
    setCount(inQueueLabel, String.valueOf(safeInt(() -> qms.getActiveQueue(deptId).size())));
    setCount(completedLabel, String.valueOf(completed));
  }

  private static void setCount(JLabel label, String value) {
    if (label != null) {
      label.setText(value);
    }
  }

  private static int safeInt(java.util.function.IntSupplier s) {
    try {
      return s.getAsInt();
    } catch (Exception ex) {
      return 0;
    }
  }

  // --- 3b. Queue + appointment history (today, dept-scoped, merged) ---

  private void refreshHistory(int deptId) {
    historyModel.setRowCount(0);
    if (deptId < 0) {
      return;
    }
    String date = LocalDate.now().toString();

    // Queue entries created today in this department (the queue side of the merged history).
    for (QueueEntry e : queueEntryDAO.findAll()) {
      if (e.getDepartmentId() != deptId) {
        continue;
      }
      if (e.getQueueDate() == null || !e.getQueueDate().equals(date)) {
        continue;
      }
      historyModel.addRow(
          new Object[] {
            date,
            UiData.patientName(e.getPatientId()),
            "Queue (" + UiData.queueLabel(departmentName(deptId), e.getQueueNumber()) + ")",
            e.getStatus()
          });
    }

    // Appointments made today in this department's services (the appointment side).
    List<Appointment> todaysAppts = deptAppointments(deptId);
    for (Appointment a : todaysAppts) {
      if (a.getAppointmentDate() == null || !a.getAppointmentDate().toString().equals(date)) {
        continue;
      }
      historyModel.addRow(
          new Object[] {
            a.getAppointmentDate().toString(),
            UiData.patientName(a.getPatientId()),
            "Appointment",
            a.getStatus()
          });
    }

    if (historyModel.getRowCount() == 0) {
      historyModel.addRow(new Object[] {"—", "No queue entries or appointments today in this department.", "", ""});
    }
  }

  // --- 3c. Upcoming appointments (SCHEDULED/CONFIRMED, date >= today, dept-scoped) ---

  private void refreshUpcoming(int deptId) {
    upcomingModel.setRowCount(0);
    if (deptId < 0) {
      return;
    }
    LocalDate today = LocalDate.now();
    List<Appointment> upcoming = new ArrayList<>();
    for (Appointment a : deptAppointments(deptId)) {
      if (a.getAppointmentDate() == null) {
        continue;
      }
      if (a.getAppointmentDate().isBefore(today)) {
        continue;
      }
      if (!isOpenStatus(a.getStatus())) {
        continue;
      }
      upcoming.add(a);
    }
    upcoming.sort((x, y) -> x.getAppointmentDate().compareTo(y.getAppointmentDate()));
    for (Appointment a : upcoming) {
      upcomingModel.addRow(
          new Object[] {
            a.getAppointmentDate() + " " + a.getAppointmentTime(),
            UiData.patientName(a.getPatientId()),
            a.getDoctorId() != null ? UiData.doctorName(a.getDoctorId()) : "Unassigned",
            a.getStatus()
          });
    }
    if (upcoming.isEmpty()) {
      upcomingModel.addRow(new Object[] {"—", "No upcoming appointments for this department.", "", ""});
    }
  }

  // --- 3d. Doctors in this department (Edit + Remove) ---

  private void refreshDoctors(int deptId) {
    doctorsModel.setRowCount(0);
    if (deptId < 0) {
      return;
    }
    List<Doctor> doctors = safeList(() -> doctorController.getDoctorsByDepartment(deptId));
    for (Doctor d : doctors) {
      doctorsModel.addRow(
          new Object[] {
            "Dr. " + d.getFirstName() + " " + d.getLastName(),
            d.getLicenseNum() == null || d.getLicenseNum().isBlank() ? "--" : d.getLicenseNum(),
            d.isActive() ? "Active" : "Inactive"
          });
      int r = doctorsModel.getRowCount() - 1;

      JButton edit = new JButton("Edit");
      styleSmallButton(edit, PRIMARY_BLUE);
      final int id = d.getDoctorId();
      edit.addActionListener(e -> editDoctor(id));

      // Remove is blocked "In use" while the doctor has open appointments (AdminDoctorsPanel pattern).
      int open = countActiveAppointments(d.getDoctorId());
      JButton remove = new JButton(open > 0 ? "In use" : "Remove");
      styleSmallButton(remove, open > 0 ? new Color(113, 128, 150) : new Color(214, 64, 64));
      if (open > 0) {
        remove.setToolTipText("This doctor has " + open + " open appointment(s). Re-assign them first.");
        remove.setEnabled(false);
      } else {
        final int rmId = d.getDoctorId();
        remove.addActionListener(e -> confirmRemove(rmId));
      }

      JPanel cellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
      cellPanel.setOpaque(false);
      cellPanel.add(edit);
      cellPanel.add(remove);
      doctorsModel.setValueAt(cellPanel, r, 3);
    }
    if (doctorsModel.getRowCount() == 0) {
      doctorsModel.addRow(new Object[] {"—", "No doctors in this department.", "", ""});
    }
  }

  /** Edits a doctor's first/last name, license, and active flag in a modal dialog. */
  private void editDoctor(int doctorId) {
    Doctor d = doctorController.getDoctor(doctorId);
    if (d == null) {
      JOptionPane.showMessageDialog(this, "Doctor no longer exists.", "Edit doctor", JOptionPane.WARNING_MESSAGE);
      refresh();
      return;
    }
    final JTextField first = new JTextField(d.getFirstName());
    final JTextField last = new JTextField(d.getLastName());
    final JTextField license = new JTextField(d.getLicenseNum() == null ? "" : d.getLicenseNum());
    final JCheckBox active = new JCheckBox("Active", d.isActive());
    active.setFont(new Font("SansSerif", Font.PLAIN, 13));

    JPanel form =
        new JPanel(
            new MigLayout("insets 24 28, fillx, gapy 14", "[left 110][grow 260, right, fill]", "[]"));
    form.setBackground(WHITE);
    form.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    form.add(new JLabel("First Name:", SwingConstants.RIGHT));
    form.add(first);
    form.add(new JLabel("Last Name:", SwingConstants.RIGHT));
    form.add(last);
    form.add(new JLabel("License No:", SwingConstants.RIGHT));
    form.add(license);
    form.add(new JLabel("Status:", SwingConstants.RIGHT));
    form.add(active);

    JButton ok = new JButton("Save");
    ok.setBackground(PRIMARY_BLUE);
    ok.setForeground(WHITE);
    ok.setFocusPainted(false);
    ok.setFont(new Font("SansSerif", Font.BOLD, 12));
    ok.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    JButton cancel = new JButton("Cancel");

    Window owner = SwingUtilities.getWindowAncestor(this);
    final JDialog dialog =
        new JDialog(owner instanceof Frame f ? f : null, "Edit Doctor — " + d.getFirstName() + " " + d.getLastName(), true);
    dialog.setBackground(BACKGROUND_LIGHT);
    dialog.setLayout(new BorderLayout(10, 10));
    ((JComponent) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    dialog.add(form, BorderLayout.CENTER);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    buttons.setOpaque(false);
    buttons.add(ok);
    buttons.add(cancel);
    dialog.add(buttons, BorderLayout.SOUTH);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.pack();
    dialog.setLocationRelativeTo(this);

    ok.addActionListener(
        e -> {
          String f = first.getText().trim();
          String l = last.getText().trim();
          if (f.isEmpty() || l.isEmpty()) {
            JOptionPane.showMessageDialog(
                dialog, "First and last name are required.", "Required fields", JOptionPane.WARNING_MESSAGE);
            return;
          }
          Doctor upd = doctorController.getDoctor(doctorId);
          if (upd == null) {
            dialog.dispose();
            refresh();
            return;
          }
          upd.setFirstName(f);
          upd.setLastName(l);
          upd.setLicenseNum(license.getText().trim());
          upd.setActive(active.isSelected());
          try {
            doctorController.updateDoctor(upd);
            dialog.dispose();
            refresh();
          } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Could not save doctor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        });
    cancel.addActionListener(e -> dialog.dispose());
    dialog.setVisible(true);
  }

  private void confirmRemove(int doctorId) {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Remove Dr. " + doctorName(doctorId) + " from this department? This cannot be undone.",
            "Remove doctor",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      doctorController.deleteDoctor(doctorId);
      refresh();
      JOptionPane.showMessageDialog(this, "Doctor removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not remove (in use by data): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // --- helpers ---

  /** All live (non-trashed) appointments scoped to this department, via the service's department. */
  private List<Appointment> deptAppointments(int deptId) {
    List<Appointment> out = new ArrayList<>();
    try {
      for (Appointment a : appointmentDAO.findAll()) {
        if (a.getServiceId() <= 0) {
          continue;
        }
        Service s = serviceDAO.findById(a.getServiceId());
        if (s == null || s.getDepartmentId() != deptId) {
          continue;
        }
        out.add(a);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return out;
  }

  private static boolean isOpenStatus(String status) {
    return "SCHEDULED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
  }

  /** Open (SCHEDULED/CONFIRMED) appointment count for a doctor — same guard AdminDoctorsPanel uses. */
  private int countActiveAppointments(int doctorId) {
    try (java.sql.Connection c = org.hospitalqueing.database.DatabaseConnection.getConnection();
        java.sql.PreparedStatement st =
            c.prepareStatement(
                "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND status IN ('SCHEDULED','CONFIRMED')")) {
      st.setInt(1, doctorId);
      try (java.sql.ResultSet rs = st.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    } catch (Exception ex) {
      return 0;
    }
  }

  private String departmentName(int deptId) {
    Department d = departmentDAO.findById(deptId);
    return d != null ? d.getDepartmentName() : "";
  }

  private String doctorName(int doctorId) {
    Doctor d = doctorController.getDoctor(doctorId);
    return d == null ? "#" + doctorId : d.getFirstName() + " " + d.getLastName();
  }

  private List<Doctor> safeList(java.util.function.Supplier<List<Doctor>> s) {
    try {
      return s.get();
    } catch (Exception ex) {
      ex.printStackTrace();
      return new ArrayList<>();
    }
  }

  private void styleSmallButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 11));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }
}
