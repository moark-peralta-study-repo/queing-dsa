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
 * Doctor dashboard (T1): what a logged-in doctor sees. The whole department's live queue and
 * today's appointments — not just the doctor's own — because a department's patients are shared
 * across its doctors. Stats are queried live (the admin dashboard's pattern), not hard-coded.
 *
 * <p>Scope: doctor login + routing + the per-doctor home. Appointment confirmation and the
 * History/Appointments quick-action tabs are T3 (see TODO.md).
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

  // Stat value labels — populated fresh on every reload.
  private JLabel todayPatientsVal;
  private JLabel inQueueVal;
  private JLabel completedVal;

  /** The live content; rebuilt on every login so the greeting + data reflect the current doctor. */
  private final JPanel contentHost = new JPanel(new BorderLayout());
  private JPanel homeScreen;

  public DoctorDashboardPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    contentHost.setBackground(BACKGROUND_LIGHT);
    homeScreen = buildHomeScreen();
    contentHost.add(homeScreen, BorderLayout.CENTER);
    add(contentHost, BorderLayout.CENTER);
  }

  /** Rebuilds the doctor home (greeting + live data) for the currently logged-in user. */
  public void reload() {
    contentHost.remove(homeScreen);
    homeScreen = buildHomeScreen();
    contentHost.add(homeScreen, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

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

    // B. Live facility stats — department-wide, queried.
    int inQueue = doctor != null ? qms.getActiveQueue(doctor.getDepartmentId()).size() : 0;
    int completed = countCompletedToday(doctor != null ? doctor.getDepartmentId() : -1);
    int todayPatients = countActiveToday(doctor != null ? doctor.getDepartmentId() : -1);

    JPanel summary = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill]", "[]"));
    summary.setOpaque(false);
    summary.add(createStatCard("Today's Patients", todayPatientsVal = new JLabel(String.valueOf(todayPatients)), "👥", new Color(230, 244, 255), PRIMARY_BLUE));
    summary.add(createStatCard("In Queue", inQueueVal = new JLabel(String.valueOf(inQueue)), "🕒", new Color(255, 244, 229), new Color(230, 126, 34)));
    summary.add(createStatCard("Completed", completedVal = new JLabel(String.valueOf(completed)), "✅", new Color(235, 249, 241), new Color(46, 204, 113)));
    panel.add(summary);

    // C. Live department queue (department-wide, shared by all doctors in the dept).
    JLabel queueLabel = new JLabel("Live Queue — " + (deptName + " (all in department)"));
    queueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    queueLabel.setForeground(TEXT_DARK);
    panel.add(queueLabel, "gaptop 10");
    panel.add(buildTable(queueTableModel));

    // D. Today's appointments (department-wide).
    JLabel apptLabel = new JLabel("Today's Appointments — " + (deptName + " (confirming arrives in T3)"));
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

  private JPanel createStatCard(String title, JLabel countLbl, String icon, Color bgColor, Color iconColor) {
    JPanel card = new JPanel(new MigLayout("insets 20, fillx", "[left]push[right]", "[]10[]"));
    card.setBackground(WHITE);
    card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLbl.setForeground(TEXT_MUTED);
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    iconLbl.setForeground(iconColor);
    countLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
    countLbl.setForeground(TEXT_DARK);
    card.add(titleLbl, "cell 0 0");
    card.add(iconLbl, "cell 1 0");
    card.add(countLbl, "cell 0 1, span 2");
    return card;
  }
}
