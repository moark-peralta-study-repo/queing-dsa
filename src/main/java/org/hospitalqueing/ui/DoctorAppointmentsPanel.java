package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hospitalqueing.controller.AppointmentController;
import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.Service;
import org.hospitalqueing.service.AppointmentService;

/**
 * Doctor dashboard section (T2): the department's current open appointments — SCHEDULED or
 * CONFIRMED — filtered by the service's department, with a Confirm button on every SCHEDULED
 * row. Department-wide on purpose: a department's bookings are shared across its doctors, and
 * confirmation is a service-level action (the same reason the home screen shows the whole
 * department's queue). Confirm goes through AppointmentController → AppointmentService, not
 * raw SQL.
 */
public class DoctorAppointmentsPanel extends JPanel {

  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);

  private final MainFrame parentFrame;
  private final DefaultTableModel tableModel =
      new DefaultTableModel(new Object[]{"Time", "Patient", "Service", "Doctor", "Status", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 5;
        }
      };
  private JTable table;

  public DoctorAppointmentsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildContent(), BorderLayout.CENTER);
    refresh();
  }

  private JPanel buildContent() {
    JPanel scrollArea = new JPanel(new MigLayout("insets 30 40 30 40, fillx", "[grow, fill]", "[]16[]"));
    scrollArea.setOpaque(false);

    Doctor doctor = UiData.doctorForUser(parentFrame.getLoggedInUser());
    String deptName = doctor != null ? deptNameFor(doctor) : "your department";

    JPanel headerCard = new JPanel(new MigLayout("insets 20 24, fillx", "[grow, left]push[right]", "[]"));
    headerCard.setBackground(WHITE);
    headerCard.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel title = new JLabel("Open Appointments — " + deptName);
    title.setFont(new Font("SansSerif", Font.BOLD, 16));
    title.setForeground(TEXT_DARK);
    JLabel sub = new JLabel("SCHEDULED / CONFIRMED bookings served by this department's services. Confirm a SCHEDULED booking to lock it in.");
    sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
    sub.setForeground(TEXT_MUTED);
    headerCard.add(title, "wrap");
    headerCard.add(sub, "push");
    scrollArea.add(headerCard);

    table = new JTable(tableModel);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.setPreferredSize(new Dimension(700, 260));
    scrollArea.add(tableScroll);

    return scrollArea;
  }

  /** Re-queries open department appointments; called on navigation and after each confirm. */
  public void refresh() {
    tableModel.setRowCount(0);
    Doctor doctor = UiData.doctorForUser(parentFrame.getLoggedInUser());
    int deptId = doctor != null ? doctor.getDepartmentId() : -1;
    List<Appointment> open = new ArrayList<>();
    try {
      for (Appointment a : new AppointmentController(new AppointmentService(new AppointmentDAO())).getAllAppointments()) {
        if (!isOpen(a.getStatus())) {
          continue;
        }
        if (deptId >= 0 && serviceDepartment(a.getServiceId()) != deptId) {
          continue;
        }
        open.add(a);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    open.sort((x, y) -> x.getAppointmentDate().compareTo(y.getAppointmentDate()));
    for (Appointment a : open) {
      tableModel.addRow(new Object[]{
          a.getAppointmentDate() + " " + a.getAppointmentTime(),
          UiData.patientName(a.getPatientId()),
          UiData.serviceName(a.getServiceId()),
          a.getDoctorId() != null ? UiData.doctorName(a.getDoctorId()) : "Unassigned",
          a.getStatus()
      });
      int r = tableModel.getRowCount() - 1;
      if ("SCHEDULED".equalsIgnoreCase(a.getStatus())) {
        JButton confirmBtn = new JButton("Confirm");
        styleSmallButton(confirmBtn, PRIMARY_BLUE);
        final int appointmentId = a.getAppointmentId();
        confirmBtn.addActionListener(e -> confirm(appointmentId));
        tableModel.setValueAt(confirmBtn, r, 5);
      } else {
        JButton doneBtn = new JButton("Confirmed");
        styleSmallButton(doneBtn, new Color(46, 204, 113));
        doneBtn.setEnabled(false);
        doneBtn.setToolTipText("Booked and locked in.");
        tableModel.setValueAt(doneBtn, r, 5);
      }
    }
    if (open.isEmpty()) {
      tableModel.addRow(new Object[]{"—", "No open appointments for this department.", "", "", "", ""});
    }
  }

  private void confirm(int appointmentId) {
    try {
      new AppointmentController(new AppointmentService(new AppointmentDAO())).confirmAppointment(appointmentId);
      refresh();
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not confirm appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private static boolean isOpen(String status) {
    return "SCHEDULED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
  }

  /** The department a service belongs to (mirrors the home screen's filter), or -1. */
  private static int serviceDepartment(int serviceId) {
    try {
      Service s = new org.hospitalqueing.dao.ServiceDAO().findById(serviceId);
      return s != null ? s.getDepartmentId() : -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  private static String deptNameFor(Doctor doctor) {
    try {
      Department dept = new org.hospitalqueing.dao.DepartmentDAO().findById(doctor.getDepartmentId());
      if (dept != null) {
        return dept.getDepartmentName();
      }
    } catch (Exception ignored) {
    }
    return "your department";
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
