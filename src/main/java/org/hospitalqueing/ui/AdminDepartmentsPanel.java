package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import org.hospitalqueing.controller.DepartmentController;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Department;

/**
 * Admin section: manage the departments table. A department that still has services or doctors
 * assigned to it cannot be deleted here — those rows would lose their department reference.
 */
public class AdminDepartmentsPanel extends JPanel {

  private final MainFrame parentFrame;
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"ID", "Department", "Doctors", "Services", "Status", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 5;
        }
      };
  private JTextField nameField;
  private JCheckBox activeCheck;

  public AdminDepartmentsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(new Color(245, 247, 250));
    add(buildHeader("DEPARTMENTS"), BorderLayout.NORTH);
    add(buildScrollableArea(), BorderLayout.CENTER);
    refresh();
  }

  /** Dark header bar with the shared "< Back" link to the admin dashboard. */
  private JPanel buildHeader(String title) {
    JPanel headerPanel = new JPanel(new MigLayout("insets 12 20 12 20, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(new Color(13, 37, 63));
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    titleLabel.setForeground(Color.WHITE);
    headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));
    headerPanel.add(titleLabel);
    return headerPanel;
  }

  private JPanel buildScrollableArea() {
    JPanel scrollArea = new JPanel(new MigLayout("insets 24 30 24 30, gap 18, fill", "[460!][grow, fill]", "[grow, fill]"));
    scrollArea.setOpaque(false);

    JPanel formCard = new JPanel(new MigLayout("insets 20 24, wrap 2, gapx 14, gapy 12", "[140!][grow, fill]"));
    formCard.setBackground(Color.WHITE);
    formCard.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));

    formCard.add(new JLabel("Add Department"));
    formCard.add(new JLabel("Department Name:", SwingConstants.RIGHT));
    nameField = new JTextField();
    formCard.add(nameField);
    formCard.add(new JLabel("Status:", SwingConstants.RIGHT));
    activeCheck = new JCheckBox("Active", true);
    formCard.add(activeCheck);

    JButton addBtn = new JButton("Add Department");
    styleButton(addBtn, new Color(21, 101, 192));
    formCard.add(addBtn, "span 2");

    scrollArea.add(formCard);

    JTable deptTable = new JTable(tableModel);
    TableButtons.renderButtons(deptTable, 5);
    JScrollPane tableScroll = new JScrollPane(deptTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    scrollArea.add(tableScroll);

    addBtn.addActionListener(e -> addDepartment());
    return scrollArea;
  }

  private void addDepartment() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter a department name.", "Required", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (UiData.departmentIdByName(name) >= 0) {
      JOptionPane.showMessageDialog(this, "A department named \"" + name + "\" already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
      return;
    }
    try {
      Department dept = new Department();
      dept.setDepartmentName(name);
      dept.setIsActive(activeCheck.isSelected());
      new DepartmentController(new org.hospitalqueing.service.DepartmentService(new DepartmentDAO())).createDepartment(dept);
      refresh();
      nameField.setText("");
      JOptionPane.showMessageDialog(this, "Department added.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Could not add department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void confirmDelete(int deptId) {
    int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + UiData.departmentName(deptId) + "\"?", "Delete department", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      new DepartmentDAO().delete(deptId);
      refresh();
      JOptionPane.showMessageDialog(this, "Department deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void refresh() {
    tableModel.setRowCount(0);
    List<Department> departments;
    try {
      departments = new DepartmentDAO().findAll();
    } catch (Exception ex) {
      return;
    }
    for (Department d : departments) {
      int doctorCount = countDoctors(d.getDepartmentId());
      int serviceCount = countServices(d.getDepartmentId());
      tableModel.addRow(new Object[]{
          d.getDepartmentId(),
          d.getDepartmentName(),
          doctorCount,
          serviceCount,
          d.getIsActive() ? "Active" : "Inactive",
      });
      int r = tableModel.getRowCount() - 1;
      JButton del = new JButton(doctorCount > 0 || serviceCount > 0 ? "In use" : "Delete");
      styleSmallButton(del, doctorCount > 0 || serviceCount > 0 ? new Color(113, 128, 150) : new Color(214, 64, 64));
      if (doctorCount > 0 || serviceCount > 0) {
        del.setToolTipText(doctorCount + " doctor(s) and " + serviceCount + " service(s) assigned. Remove those first.");
        del.setEnabled(false);
      } else {
        del.addActionListener(e -> confirmDelete(d.getDepartmentId()));
      }
      tableModel.setValueAt(del, r, 5);
    }
  }

  private int countDoctors(int deptId) {
    try {
      return new DoctorDAO().findByDepartment(deptId).size();
    } catch (Exception ex) {
      return 0;
    }
  }

  private int countServices(int deptId) {
    try {
      int count = 0;
      for (org.hospitalqueing.model.Service s : new ServiceDAO().findAll()) {
        if (s.getDepartmentId() == deptId) {
          count++;
        }
      }
      return count;
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
