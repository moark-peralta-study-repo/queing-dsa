package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import org.hospitalqueing.controller.ServiceController;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Service;

/**
 * Admin section: manage the services catalog. Services appear in the patient "Book Appointment"
 * form; a service with open appointments cannot be deleted (those rows would lose their
 * service reference).
 */
public class AdminServicesPanel extends JPanel {

  private final MainFrame parentFrame;
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"ID", "Service", "Department", "Avg (min)", "Status", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false; // button column: no text editor on double-click
        }
      };
  private JComboBox<String> departmentCombo;
  private JTextField nameField;
  private JTextField minutesField;

  public AdminServicesPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(new Color(245, 247, 250));
    add(buildHeader("SERVICES"), BorderLayout.NORTH);
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
    JPanel scrollArea = new JPanel(new MigLayout("insets 24 30 24 30, gap 18, fill", "[520!][grow, fill]", "[grow, fill]"));
    scrollArea.setOpaque(false);

    JPanel formCard = new JPanel(new MigLayout("insets 20 24, wrap 4, gapx 14, gapy 12", "[110!][grow 260][grow 180][grow 140]"));
    formCard.setBackground(Color.WHITE);
    formCard.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));

    formCard.add(new JLabel("Add Service"));
    formCard.add(new JLabel("Department:", SwingConstants.RIGHT));
    formCard.add(new JLabel("Service Name:", SwingConstants.RIGHT));
    formCard.add(new JLabel("Avg Minutes:", SwingConstants.RIGHT));

    departmentCombo = new JComboBox<>(UiData.departmentNames());
    nameField = new JTextField();
    minutesField = new JTextField("20");
    formCard.add(departmentCombo);
    formCard.add(nameField);
    formCard.add(minutesField);

    JButton addBtn = new JButton("Add Service");
    styleButton(addBtn, new Color(21, 101, 192));
    formCard.add(addBtn, "span 4");

    scrollArea.add(formCard);

    JTable theTable = new JTable(tableModel);
    TableButtons.renderButtons(theTable, 5);
    JScrollPane tableScroll = new JScrollPane(theTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    scrollArea.add(tableScroll);

    addBtn.addActionListener(e -> addService());
    return scrollArea;
  }

  private void addService() {
    String deptName = (String) departmentCombo.getSelectedItem();
    int deptId = UiData.departmentIdByName(deptName);
    if (deptId < 0) {
      JOptionPane.showMessageDialog(this, "Pick a department first (add one in the Departments tab if it's missing).", "Missing department", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter a service name.", "Required", JOptionPane.WARNING_MESSAGE);
      return;
    }
    int minutes;
    try {
      minutes = Integer.parseInt(minutesField.getText().trim());
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Average minutes must be a whole number.", "Invalid", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (minutes < 1) {
      JOptionPane.showMessageDialog(this, "Average minutes must be at least 1.", "Invalid", JOptionPane.WARNING_MESSAGE);
      return;
    }
    try {
      Service service = new Service();
      service.setDepartmentId(deptId);
      service.setServiceName(name);
      service.setAvgServiceMinutes(minutes);
      service.setActive(true);
      new ServiceController(new org.hospitalqueing.service.ServiceCatalogService(new ServiceDAO())).createService(service);
      refresh();
      nameField.setText("");
      minutesField.setText("20");
      JOptionPane.showMessageDialog(this, "Service added.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Could not add service (name must be unique per department): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void confirmDelete(int serviceId) {
    int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + UiData.serviceName(serviceId) + "\"?", "Delete service", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      new ServiceDAO().delete(serviceId);
      refresh();
      JOptionPane.showMessageDialog(this, "Service deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not delete (in use by data): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void refresh() {
    tableModel.setRowCount(0);
    List<Service> services;
    try {
      services = new ServiceDAO().findAll();
    } catch (Exception ex) {
      return;
    }
    for (Service s : services) {
      int open = countOpenAppointments(s.getServiceId());
      tableModel.addRow(new Object[]{
          s.getServiceId(),
          s.getServiceName(),
          UiData.departmentName(s.getDepartmentId()),
          s.getAvgServiceMinutes(),
          s.isActive() ? "Active" : "Inactive",
      });
      int r = tableModel.getRowCount() - 1;
      JButton del = new JButton(open > 0 ? "In use" : "Delete");
      styleSmallButton(del, open > 0 ? new Color(113, 128, 150) : new Color(214, 64, 64));
      if (open > 0) {
        del.setToolTipText("This service has " + open + " open appointment(s). Re-assign them first.");
        del.setEnabled(false);
      } else {
        del.addActionListener(e -> confirmDelete(s.getServiceId()));
      }
      tableModel.setValueAt(del, r, 5);
    }
  }

  private int countOpenAppointments(int serviceId) {
    try (java.sql.Connection c = org.hospitalqueing.database.DatabaseConnection.getConnection();
        java.sql.PreparedStatement st =
            c.prepareStatement(
                "SELECT COUNT(*) FROM appointments WHERE service_id = ? AND status IN ('SCHEDULED','CONFIRMED')")) {
      st.setInt(1, serviceId);
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
