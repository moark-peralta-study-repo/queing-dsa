package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import org.hospitalqueing.controller.DoctorController;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.model.Doctor;

/**
 * Admin section: manage the doctors table. Adding a doctor populates the patient appointment
 * "Preferred Doctor" dropdown; deleting one that still has appointments is blocked (the
 * appointments row would lose its doctor reference).
 */
public class AdminDoctorsPanel extends JPanel {

  private final MainFrame parentFrame;

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"ID", "Name", "Department", "License", "Status", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 5;
        }
      };
  private JComboBox<String> departmentCombo;
  private JTextField firstNameField;
  private JTextField lastNameField;
  private JTextField licenseField;

  public AdminDoctorsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(new Color(245, 247, 250));
    add(buildScrollableArea(), BorderLayout.CENTER);
    refresh();
  }

  private JPanel buildScrollableArea() {
    JPanel scrollArea = new JPanel(new MigLayout("insets 30 40 30 40, fillx", "[grow, fill]", "[]16[]"));
    scrollArea.setOpaque(false);

    JPanel formCard = new JPanel(new MigLayout("insets 20 24, wrap 4, gapx 14, gapy 12", "[110!][grow 260][grow 180][grow 140]"));
    formCard.setBackground(Color.WHITE);
    formCard.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));

    formCard.add(new JLabel("Add Doctor"));
    formCard.add(new JLabel("Department:", SwingConstants.RIGHT));
    formCard.add(new JLabel("First Name:", SwingConstants.RIGHT));
    formCard.add(new JLabel("Last Name:", SwingConstants.RIGHT));

    departmentCombo = new JComboBox<>(UiData.departmentNames());
    firstNameField = new JTextField();
    lastNameField = new JTextField();
    formCard.add(departmentCombo);
    formCard.add(firstNameField);
    formCard.add(lastNameField);

    formCard.add(new JLabel("License No:", SwingConstants.RIGHT));
    licenseField = new JTextField();
    JButton addBtn = new JButton("Add Doctor");
    styleButton(addBtn, new Color(21, 101, 192));
    formCard.add(licenseField);
    formCard.add(addBtn);

    scrollArea.add(formCard);

    JScrollPane tableScroll = new JScrollPane(new JTable(tableModel));
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    scrollArea.add(tableScroll);

    addBtn.addActionListener(e -> addDoctor());
    return scrollArea;
  }

  private void addDoctor() {
    String deptName = (String) departmentCombo.getSelectedItem();
    int deptId = UiData.departmentIdByName(deptName);
    if (deptId < 0) {
      JOptionPane.showMessageDialog(this, "Pick a department first (add one in the Departments tab if it's missing).", "Missing department", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String first = firstNameField.getText().trim();
    String last = lastNameField.getText().trim();
    if (first.isEmpty() || last.isEmpty()) {
      JOptionPane.showMessageDialog(this, "First and last name are required.", "Required fields", JOptionPane.WARNING_MESSAGE);
      return;
    }
    try {
      Doctor doctor = new Doctor();
      doctor.setDepartmentId(deptId);
      doctor.setFirstName(first);
      doctor.setLastName(last);
      doctor.setLicenseNum(licenseField.getText().trim());
      doctor.setActive(true);
      new DoctorController(new org.hospitalqueing.service.DoctorService(new DoctorDAO())).createDoctor(doctor);
      refresh();
      clearForm();
      JOptionPane.showMessageDialog(this, "Doctor added: " + doctorName(doctor.getDoctorId()), "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Could not add doctor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearForm() {
    departmentCombo.setSelectedIndex(0);
    firstNameField.setText("");
    lastNameField.setText("");
    licenseField.setText("");
  }

  private String doctorName(int doctorId) {
    Doctor d = new DoctorDAO().findById(doctorId);
    return d == null ? "Doctor #" + doctorId : "Dr. " + d.getFirstName() + " " + d.getLastName();
  }

  public void refresh() {
    tableModel.setRowCount(0);
    List<Doctor> doctors;
    try {
      doctors = new DoctorDAO().findAll();
    } catch (Exception ex) {
      return;
    }
    for (Doctor d : doctors) {
      int activeAppointments = countActiveAppointments(d.getDoctorId());
      Object[] row = {
        d.getDoctorId(),
        doctorName(d.getDoctorId()),
        UiData.departmentName(d.getDepartmentId()),
        d.getLicenseNum() == null || d.getLicenseNum().isBlank() ? "--" : d.getLicenseNum(),
        d.isActive() ? "Active" : "Inactive",
      };
      tableModel.addRow(row);
      int r = tableModel.getRowCount() - 1;
      JButton del = new JButton(activeAppointments > 0 ? "In use" : "Delete");
      styleSmallButton(del, activeAppointments > 0 ? new Color(113, 128, 150) : new Color(214, 64, 64));
      if (activeAppointments > 0) {
        del.setToolTipText("This doctor has " + activeAppointments + " open appointment(s). Re-assign them first.");
        del.setEnabled(false);
      } else {
        del.addActionListener(e -> confirmDelete(d.getDoctorId()));
      }
      tableModel.setValueAt(del, r, 5);
    }
  }

  private void confirmDelete(int doctorId) {
    int choice = JOptionPane.showConfirmDialog(this, "Delete " + doctorName(doctorId) + "? This cannot be undone.", "Delete doctor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      new DoctorDAO().delete(doctorId);
      refresh();
      JOptionPane.showMessageDialog(this, "Doctor deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not delete (in use by data): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

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

  private void styleButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 13));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
