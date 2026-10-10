package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueStatus;
import org.hospitalqueing.service.QueueManagementService;

/**
 * Doctor dashboard: what a logged-in doctor sees. Dark header nav (DASHBOARD | APPOINTMENTS |
 * HISTORY, admin-dashboard style) over an internal CardLayout of sections:
 *
 * <ul>
 *   <li>HOME (T1) — the department's live stats, queue, and today's appointments.</li>
 *   <li>APPOINTMENTS (T2, {@link DoctorAppointmentsPanel}) — the department's open
 *       SCHEDULED/CONFIRMED bookings, each SCHEDULED row confirmable.</li>
 *   <li>HISTORY (T3, {@link DoctorHistoryPanel}) — the department's closed bookings, read-only.</li>
 * </ul>
 *
 * <p>Everything is department-wide on purpose: a department's patients and bookings are shared
 * across its doctors. MainFrame routes doctor logins here (DOCTOR_DASHBOARD card).
 */
public class DoctorDashboardPanel extends JPanel {

  private final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final MainFrame parentFrame;
  private final QueueManagementService qms =
      new QueueManagementService(
          new QueueEntryDAO(), new QueueEventDAO(), new ServiceDAO(), new DepartmentDAO(), new CounterDAO());

  private final DefaultTableModel queueTableModel =
      new DefaultTableModel(new Object[]{"Ticket", "Patient", "Department", "Joined", "Status"}, 0);
  private final DefaultTableModel appointmentTableModel =
      new DefaultTableModel(new Object[]{"Time", "Patient", "Service", "Status"}, 0);

  private final CardLayout doctorCardLayout = new CardLayout();
  private final JPanel doctorContentPanel;

  private final DoctorAppointmentsPanel appointmentsPanel;
  private final DoctorHistoryPanel historyPanel;
  private JPanel homeScreen;

  /** The live nav links; DASHBOARD is active by default and re-lit on each switch. */
  private JLabel homeNav;
  private JLabel appointmentsNav;
  private JLabel historyNav;

  public DoctorDashboardPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    // --- 1. DOCTOR HEADER (Dark Blue Nav) ---
    JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]push[center]25[center]25[center]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(new JLabel(" "), "cell 0 0");

    homeNav = createHeaderLink("DASHBOARD", true);
    appointmentsNav = createHeaderLink("APPOINTMENTS", false);
    historyNav = createHeaderLink("HISTORY", false);

    headerPanel.add(homeNav, "cell 1 0");
    headerPanel.add(appointmentsNav, "cell 2 0");
    headerPanel.add(historyNav, "cell 3 0");

    JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    rightControls.setOpaque(false);
    JButton logoutBtn = new JButton("Logout");
    logoutBtn.setBackground(WHITE);
    logoutBtn.setForeground(HEADER_DARK_BLUE);
    logoutBtn.setFocusPainted(false);
    logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
    logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    rightControls.add(logoutBtn);
    headerPanel.add(rightControls, "cell 4 0");

    add(headerPanel, BorderLayout.NORTH);

    // --- 2. CONTENT (internal CardLayout) ---
    doctorContentPanel = new JPanel(doctorCardLayout);
    doctorContentPanel.setBackground(BACKGROUND_LIGHT);

    appointmentsPanel = new DoctorAppointmentsPanel(parentFrame);
    historyPanel = new DoctorHistoryPanel(parentFrame);

    homeScreen = buildHomeScreen();
    doctorContentPanel.add(homeScreen, "DOCTOR_HOME");
    doctorContentPanel.add(appointmentsPanel, "DOCTOR_APPOINTMENTS");
    doctorContentPanel.add(historyPanel, "DOCTOR_HISTORY");

    add(doctorContentPanel, BorderLayout.CENTER);

    // --- 3. NAV WIRING ---
    homeNav.addMouseListener(onClick(() -> showDoctor("DOCTOR_HOME")));
    appointmentsNav.addMouseListener(onClick(() -> showDoctor("DOCTOR_APPOINTMENTS")));
    historyNav.addMouseListener(onClick(() -> showDoctor("DOCTOR_HISTORY")));

    logoutBtn.addActionListener(e -> {
      int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION) {
        recordLogoutEvent();
        parentFrame.triggerLogout();
      }
    });
  }

  /** Security logs: record the logout (best-effort, never blocks logout). */
  private void recordLogoutEvent() {
    try {
      org.hospitalqueing.model.User user = parentFrame.getLoggedInUser();
      if (user == null) return;
      String role = "";
      try {
        org.hospitalqueing.model.Role r = new org.hospitalqueing.dao.RoleDAO().findById(user.getRoleId());
        if (r != null) role = r.getRoleName();
      } catch (Exception ignored) {}
      new org.hospitalqueing.dao.SecurityLogDAO(org.hospitalqueing.database.DatabaseConnection.getSingleton())
          .insert(new org.hospitalqueing.model.SecurityLog(
              org.hospitalqueing.model.SecurityLog.LOGOUT, user.getUsername(), role, true));
    } catch (Exception ignored) {}
  }

  /** Switch doctor section (header nav + quick-action cards). The home section rebuilds so its stats stay live. */
  public void showDoctor(String card) {
    if ("DOCTOR_HOME".equals(card) && homeScreen != null) {
      doctorContentPanel.remove(homeScreen);
      homeScreen = buildHomeScreen();
      doctorContentPanel.add(homeScreen, "DOCTOR_HOME");
      revalidate();
      repaint();
    }
    if ("DOCTOR_APPOINTMENTS".equals(card)) {
      appointmentsPanel.refresh();
    }
    if ("DOCTOR_HISTORY".equals(card)) {
      historyPanel.refresh();
    }
    setNavActive(card);
    doctorCardLayout.show(doctorContentPanel, card);
  }

  /** Re-activates the nav link for the shown card (hover state otherwise keeps the old color). */
  private void setNavActive(String card) {
    homeNav.setForeground("DOCTOR_HOME".equals(card) ? WHITE : new Color(150, 170, 190));
    appointmentsNav.setForeground("DOCTOR_APPOINTMENTS".equals(card) ? WHITE : new Color(150, 170, 190));
    historyNav.setForeground("DOCTOR_HISTORY".equals(card) ? WHITE : new Color(150, 170, 190));
  }

  /** Rebuilds the doctor home (greeting + live data) for the currently logged-in user. Called by MainFrame on login. */
  public void reload() {
    if (homeScreen != null) {
      doctorContentPanel.remove(homeScreen);
    }
    homeScreen = buildHomeScreen();
    doctorContentPanel.add(homeScreen, "DOCTOR_HOME");
    revalidate();
    repaint();
  }

  private java.awt.event.MouseListener onClick(Runnable action) {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        action.run();
      }
    };
  }

  // --- HOME SCREEN (T1 content, unchanged except the section quick-actions) ---
  private JPanel buildHomeScreen() {
    Doctor doctor = UiData.doctorForUser(parentFrame.getLoggedInUser());
    Department dept = null;
    if (doctor != null) {
      dept = new DepartmentDAO().findById(doctor.getDepartmentId());
    }
    String deptName = dept != null ? dept.getDepartmentName() : (doctor != null ? "your department" : "—");

    JPanel panel = new JPanel(new MigLayout("wrap 1, insets 30 40 30 40, fillx", "[grow, fill]", "[]30[]30[]"));
    panel.setBackground(BACKGROUND_LIGHT);

    // A. Greeting — the actually logged-in doctor.
    String doctorName = "Doctor";
    if (doctor != null) {
      doctorName = "Dr. " + (doctor.getFirstName() == null ? "" : doctor.getFirstName().trim())
          + " " + (doctor.getLastName() == null ? "" : doctor.getLastName().trim()).trim();
    }
    JPanel greeting = new JPanel(new MigLayout("insets 0", "[left]", "[]2[]"));
    greeting.setOpaque(false);
    JLabel g1 = new JLabel("Good day,");
    g1.setFont(new Font("SansSerif", Font.PLAIN, 16));
    g1.setForeground(TEXT_MUTED);
    JLabel g2 = new JLabel(doctorName);
    g2.setFont(new Font("SansSerif", Font.BOLD, 24));
    g2.setForeground(TEXT_DARK);
    JLabel g3 = new JLabel("Department: " + deptName + " • On duty");
    g3.setFont(new Font("SansSerif", Font.PLAIN, 14));
    g3.setForeground(TEXT_MUTED);
    greeting.add(g1, "wrap");
    greeting.add(g2, "wrap");
    greeting.add(g3);
    panel.add(greeting);

    // B. Live department stats.
    int inQueue = doctor != null ? qms.getActiveQueue(doctor.getDepartmentId()).size() : 0;
    int completed = countCompletedToday(doctor != null ? doctor.getDepartmentId() : -1);
    int todayPatients = countActiveToday(doctor != null ? doctor.getDepartmentId() : -1);

    JPanel summary = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill]", "[]"));
    summary.setOpaque(false);
    summary.add(createStatCard("Today's Patients", String.valueOf(todayPatients), "👥", new Color(230, 244, 255), PRIMARY_BLUE));
    summary.add(createStatCard("In Queue", String.valueOf(inQueue), "🕒", new Color(255, 244, 229), new Color(230, 126, 34)));
    summary.add(createStatCard("Completed", String.valueOf(completed), "✅", new Color(235, 249, 241), new Color(46, 204, 113)));
    panel.add(summary);

    // C. Department quick actions (T2/T3 sections, admin-dashboard style).
    JLabel actionsLabel = new JLabel("Appointments");
    actionsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    actionsLabel.setForeground(TEXT_DARK);
    panel.add(actionsLabel, "gaptop 10");

    JPanel actionsContainer = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill]", "[]"));
    actionsContainer.setOpaque(false);
    actionsContainer.add(createActionCard("Open Bookings", "Confirm SCHEDULED appointments for the department", "📅", "DOCTOR_APPOINTMENTS"));
    actionsContainer.add(createActionCard("History", "Completed, cancelled & no-show bookings (read-only)", "🗂️", "DOCTOR_HISTORY"));
    panel.add(actionsContainer);

    // D. Live department queue (department-wide, shared by all doctors in the dept).
    JLabel queueLabel = new JLabel("Live Queue — " + (deptName + " (all in department)"));
    queueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    queueLabel.setForeground(TEXT_DARK);
    panel.add(queueLabel, "gaptop 10");
    panel.add(buildTable(queueTableModel));

    // E. Today's appointments (department-wide).
    JLabel apptLabel = new JLabel("Today's Appointments — " + (deptName + " (open bookings on the APPOINTMENTS section)"));
    apptLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    apptLabel.setForeground(TEXT_DARK);
    panel.add(apptLabel, "gaptop 10");
    panel.add(buildTable(appointmentTableModel));

    // Populate live data.
    if (doctor != null) {
      populateQueue(doctor.getDepartmentId());
      populateAppointments(doctor.getDepartmentId());
    } else {
      populateQueue(-1);
      populateAppointments(-1);
    }

    return panel;
  }

  private void populateQueue(int deptId) {
    queueTableModel.setRowCount(0);
    if (deptId < 0) {
      return;
    }
    for (QueueEntry qe : qms.getActiveQueue(deptId)) {
      queueTableModel.addRow(new Object[]{
          UiData.queueLabel(UiData.departmentName(qe.getDepartmentId()), qe.getQueueNumber()),
          UiData.patientName(qe.getPatientId()),
          UiData.departmentName(qe.getDepartmentId()),
          qe.getJoinedAt(),
          qe.getStatus()
      });
    }
  }

  private void populateAppointments(int deptId) {
    appointmentTableModel.setRowCount(0);
    try {
      LocalDate today = LocalDate.now();
      for (Appointment appt : new AppointmentDAO().findAll()) {
        if (appt.getAppointmentDate() == null || !appt.getAppointmentDate().equals(today)) {
          continue;
        }
        // Department-wide: every appointment served by a service in this department.
        int svcDept = -1;
        org.hospitalqueing.model.Service svc = new ServiceDAO().findById(appt.getServiceId());
        if (svc != null) {
          svcDept = svc.getDepartmentId();
        }
        if (deptId >= 0 && svcDept != deptId) {
          continue;
        }
        appointmentTableModel.addRow(new Object[]{
            appt.getAppointmentTime(),
            UiData.patientName(appt.getPatientId()),
            UiData.serviceName(appt.getServiceId()),
            appt.getStatus()
        });
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private int countActiveToday(int deptId) {
    if (deptId < 0) {
      return 0;
    }
    try {
      String today = LocalDate.now().toString();
      int count = 0;
      for (QueueEntry qe : new QueueEntryDAO().findAll()) {
        if (qe.getDepartmentId() == deptId && today.equals(qe.getQueueDate())) {
          count++;
        }
      }
      return count;
    } catch (Exception ex) {
      return 0;
    }
  }

  private int countCompletedToday(int deptId) {
    if (deptId < 0) {
      return 0;
    }
    try {
      String today = LocalDate.now().toString();
      int count = 0;
      for (QueueEntry qe : new QueueEntryDAO().findAll()) {
        if (qe.getDepartmentId() == deptId && today.equals(qe.getQueueDate())
            && QueueStatus.isTerminal(qe.getStatus())) {
          count++;
        }
      }
      return count;
    } catch (Exception ex) {
      return 0;
    }
  }

  private JComponent buildTable(DefaultTableModel model) {
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    JScrollPane sp = new JScrollPane(table);
    sp.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    sp.setPreferredSize(new Dimension(700, 140));
    return sp;
  }

  // --- UI HELPERS (same style language as AdminDashboardPanel) ---
  private JLabel createHeaderLink(String text, boolean isActive) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 14));
    label.setForeground(isActive ? WHITE : new Color(150, 170, 190));
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) { label.setForeground(WHITE); }
      public void mouseExited(MouseEvent e) { if (!isActive) label.setForeground(new Color(150, 170, 190)); }
    });
    return label;
  }

  private JPanel createStatCard(String title, String count, String icon, Color bgColor, Color iconColor) {
    JPanel card = new JPanel(new MigLayout("insets 20, fillx", "[left]push[right]", "[]10[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLbl.setForeground(TEXT_MUTED);
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    iconLbl.setForeground(iconColor);
    JLabel countLbl = new JLabel(count);
    countLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
    countLbl.setForeground(TEXT_DARK);
    card.add(titleLbl, "cell 0 0");
    card.add(iconLbl, "cell 1 0");
    card.add(countLbl, "cell 0 1, span 2");
    return card;
  }

  private JPanel createActionCard(String title, String desc, String icon, String targetScreen) {
    JPanel card = new JPanel(new MigLayout("wrap 1, insets 20", "[center]", "[]10[]5[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
    titleLbl.setForeground(PRIMARY_BLUE);
    JLabel descLbl = new JLabel(desc);
    descLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
    descLbl.setForeground(TEXT_MUTED);
    card.add(iconLbl);
    card.add(titleLbl);
    card.add(descLbl);
    card.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 249, 255)); }
      public void mouseExited(MouseEvent e) { card.setBackground(WHITE); }
      public void mouseClicked(MouseEvent e) {
        showDoctor(targetScreen);
      }
    });
    return card;
  }
}
