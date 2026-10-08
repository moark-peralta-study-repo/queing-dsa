package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
 * Doctor dashboard section (T3): the department's closed-out appointment history — COMPLETED,
 * CANCELLED, and NO_SHOW — as a read-only table. No actions here: confirmations live on the
 * Appointments section, and re-opening a booking is a staff/admin concern.
 */
public class DoctorHistoryPanel extends JPanel {

  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final MainFrame parentFrame;
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"Date", "Patient", "Service", "Doctor", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false; // read-only
        }
      };

  public DoctorHistoryPanel(MainFrame parentFrame) {
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

    JPanel headerCard = new JPanel(new MigLayout("insets 20 24, fillx", "[grow, left]", "[]"));
    headerCard.setBackground(WHITE);
    headerCard.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    JLabel title = new JLabel("Appointment History — " + deptName);
    title.setFont(new Font("SansSerif", Font.BOLD, 16));
    title.setForeground(TEXT_DARK);
    headerCard.add(title);
    scrollArea.add(headerCard);

    JTable table = new JTable(tableModel);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.setPreferredSize(new Dimension(700, 260));
    scrollArea.add(tableScroll);

    return scrollArea;
  }

  /** Re-queries closed department appointments; called on navigation. */
  public void refresh() {
    tableModel.setRowCount(0);
    Doctor doctor = UiData.doctorForUser(parentFrame.getLoggedInUser());
    int deptId = doctor != null ? doctor.getDepartmentId() : -1;
    List<Appointment> closed = new ArrayList<>();
    try {
      for (Appointment a : new AppointmentController(new AppointmentService(new AppointmentDAO())).getAllAppointments()) {
        if (!isClosed(a.getStatus())) {
          continue;
        }
        if (deptId >= 0 && serviceDepartment(a.getServiceId()) != deptId) {
          continue;
        }
        closed.add(a);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    closed.sort((x, y) -> y.getAppointmentDate().compareTo(x.getAppointmentDate()));
    for (Appointment a : closed) {
      tableModel.addRow(new Object[]{
          a.getAppointmentDate() + " " + a.getAppointmentTime(),
          UiData.patientName(a.getPatientId()),
          UiData.serviceName(a.getServiceId()),
          a.getDoctorId() != null ? UiData.doctorName(a.getDoctorId()) : "Unassigned",
          a.getStatus()
      });
    }
    if (closed.isEmpty()) {
      tableModel.addRow(new Object[]{"—", "No completed, cancelled, or no-show appointments for this department.", "", "", ""});
    }
  }

  private static boolean isClosed(String status) {
    return "COMPLETED".equalsIgnoreCase(status)
        || "CANCELLED".equalsIgnoreCase(status)
        || "NO_SHOW".equalsIgnoreCase(status);
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
}
