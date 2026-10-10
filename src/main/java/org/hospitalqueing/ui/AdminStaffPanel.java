package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import org.hospitalqueing.controller.StaffController;
import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.Staff;
import org.hospitalqueing.model.User;
import org.hospitalqueing.service.AuthenticationService;

/**
 * Admin section: manage the staff table. Creating a staff member also creates their login
 * account (the staff table requires a user row). Deleting a staff member removes the link and
 * their account, so they can no longer log in.
 */
public class AdminStaffPanel extends JPanel {

  private final MainFrame parentFrame;
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"ID", "Name", "Login", "Department", "Role", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 5;
        }
      };
  private JTextField firstNameField;
  private JTextField lastNameField;
  private JTextField loginField;
  private JComboBox<String> roleCombo;
  private JComboBox<String> departmentCombo;

  public AdminStaffPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(new Color(245, 247, 250));
    add(buildHeader("STAFF"), BorderLayout.NORTH);
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

    formCard.add(new JLabel("Add Staff"));
    formCard.add(new JLabel("First Name:", SwingConstants.RIGHT));
    formCard.add(new JLabel("Last Name:", SwingConstants.RIGHT));
    formCard.add(new JLabel("Login (user):", SwingConstants.RIGHT));
    firstNameField = new JTextField();
    lastNameField = new JTextField();
    loginField = new JTextField();
    formCard.add(firstNameField);
    formCard.add(lastNameField);
    formCard.add(loginField);

    formCard.add(new JLabel("Role:", SwingConstants.RIGHT));
    roleCombo = new JComboBox<>(new String[]{"STAFF", "DEPARTMENT_STAFF", "ADMIN"});
    formCard.add(roleCombo);
    formCard.add(new JLabel("Department:", SwingConstants.RIGHT));
    departmentCombo = new JComboBox<>(UiData.departmentNames());
    formCard.add(departmentCombo);

    JButton addBtn = new JButton("Add Staff");
    styleButton(addBtn, new Color(21, 101, 192));
    formCard.add(addBtn, "span 4");

    scrollArea.add(formCard);

    JTable theTable = new JTable(tableModel);
    TableButtons.renderButtons(theTable, 5);
    JScrollPane tableScroll = new JScrollPane(theTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    scrollArea.add(tableScroll);

    addBtn.addActionListener(e -> addStaff());
    return scrollArea;
  }

  private void addStaff() {
    String first = firstNameField.getText().trim();
    String last = lastNameField.getText().trim();
    String login = loginField.getText().trim();
    if (first.isEmpty() || last.isEmpty() || login.isEmpty()) {
      JOptionPane.showMessageDialog(this, "First name, last name, and login are required.", "Required fields", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (new UserDAO().findByUsername(login) != null) {
      JOptionPane.showMessageDialog(this, "Login \"" + login + "\" is already taken.", "Duplicate login", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String role = (String) roleCombo.getSelectedItem();
    int roleId = UiData.roleIdByName(role);
    if (roleId < 0) {
      JOptionPane.showMessageDialog(this, "Role \"" + role + "\" not found.", "Invalid role", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String deptName = (String) departmentCombo.getSelectedItem();
    Integer deptId = (deptName == null || deptName.isEmpty()) ? null : UiData.departmentIdByName(deptName);
    try {
      User user = new User();
      user.setUsername(login);
      user.setPasswordHash(new AuthenticationService(new UserDAO()).hashPassword(login));
      user.setRoleId(roleId);
      user.setActive(true);
      new UserControllerHolder().create(user);
      int userId = user.getUserId();
      Staff staff = new Staff();
      staff.setUserId(userId);
      staff.setFirstName(first);
      staff.setLastName(last);
      staff.setDepartmentId(deptId);
      new StaffController(new org.hospitalqueing.service.StaffService(new StaffDAO())).createStaff(staff);
      refresh();
      clearForm();
      JOptionPane.showMessageDialog(this, "Staff added. Login: " + login + " (password = login for now).", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Could not add staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearForm() {
    firstNameField.setText("");
    lastNameField.setText("");
    loginField.setText("");
    roleCombo.setSelectedIndex(0);
    departmentCombo.setSelectedIndex(0);
  }

  private void confirmDelete(int staffId) {
    Staff target = new StaffDAO().findById(staffId);
    int choice = JOptionPane.showConfirmDialog(
        this,
        "Remove staff " + (target == null ? "#" + staffId : target.getFirstName() + " " + target.getLastName())
            + "? Their login will stop working.",
        "Remove staff", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      new StaffDAO().delete(staffId);
      if (target != null && target.getUserId() > 0) {
        new UserDAO().delete(target.getUserId());
      }
      refresh();
      JOptionPane.showMessageDialog(this, "Staff removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      refresh();
      JOptionPane.showMessageDialog(this, "Could not remove: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void refresh() {
    tableModel.setRowCount(0);
    List<Staff> staff;
    try {
      staff = new StaffDAO().findAll();
    } catch (Exception ex) {
      return;
    }
    for (Staff s : staff) {
      User u = s.getUserId() > 0 ? new UserDAO().findById(s.getUserId()) : null;
      String role = u != null ? UiData.roleNameForUser(u) : "--";
      tableModel.addRow(new Object[]{
          s.getStaffId(),
          s.getFirstName() + " " + s.getLastName(),
          u != null ? u.getUsername() : "--",
          s.getDepartmentId() == null ? "--" : UiData.departmentName(s.getDepartmentId()),
          role == null ? "--" : role,
      });
      int r = tableModel.getRowCount() - 1;
      JButton del = new JButton("Remove");
      styleSmallButton(del, new Color(214, 64, 64));
      del.addActionListener(e -> confirmDelete(s.getStaffId()));
      tableModel.setValueAt(del, r, 5);
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

  /** Tiny wrapper so the add-flow reads top-down. */
  private static class UserControllerHolder {
    private final org.hospitalqueing.controller.UserController controller =
        new org.hospitalqueing.controller.UserController(
            new org.hospitalqueing.service.UserService(new UserDAO()));

    void create(User user) {
      controller.createUser(user);
    }
  }
}
