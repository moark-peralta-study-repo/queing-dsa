package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hospitalqueing.controller.DoctorController;
import org.hospitalqueing.controller.PatientController;
import org.hospitalqueing.controller.StaffController;
import org.hospitalqueing.controller.UserController;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.RoleDAO;
import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.Patient;
import org.hospitalqueing.model.Role;
import org.hospitalqueing.model.Staff;
import org.hospitalqueing.model.User;
import org.hospitalqueing.service.AuthenticationService;

/**
 * Admin section: the full history of ALL accounts ever made. Every row can be edited (username,
 * role, active flag, password reset, and the linked patient/doctor/staff profile fields when the
 * account has one) or moved to the trash bin (soft delete — the linked patient row is trashed
 * with it; restore/permanent-delete happens from the Trash Bin panel).
 */
public class AdminAccountsPanel extends JPanel {

  private static final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private static final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private static final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private static final Color WHITE = Color.WHITE;
  private static final Color TEXT_DARK = new Color(45, 55, 72);
  private static final Color DANGER = new Color(214, 64, 64);

  private static final DateTimeFormatter CREATED_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final MainFrame parentFrame;
  private final UserDAO userDAO = new UserDAO();
  private final PatientDAO patientDAO = new PatientDAO();
  private final DoctorDAO doctorDAO = new DoctorDAO();
  private final StaffDAO staffDAO = new StaffDAO();

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[]{"Username", "Role", "Name", "Active", "Created", "", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 5 || column == 6;
        }
      };
  private JTable accountsTable;

  public AdminAccountsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);
    add(buildHeader(), BorderLayout.NORTH);
    add(buildScrollableArea(), BorderLayout.CENTER);
    loadAccountsData();
  }

  /** Dark header bar: "< Back" to the admin dashboard (left) + the "ACCOUNTS" title. */
  private JPanel buildHeader() {
    JPanel headerPanel =
        new JPanel(
            new MigLayout(
                "insets 12 20 12 20, aligny center", "[left]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"));
    JLabel titleLabel = new JLabel("ACCOUNTS");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    titleLabel.setForeground(WHITE);
    headerPanel.add(titleLabel);
    return headerPanel;
  }

  private JPanel buildScrollableArea() {
    JPanel scrollArea =
        new JPanel(
            new MigLayout("insets 30 40 30 40, fillx", "[grow, fill]", "[]10[]10[]"));
    scrollArea.setOpaque(false);

    JLabel pageTitle = new JLabel("All Accounts");
    pageTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
    pageTitle.setForeground(TEXT_DARK);
    JLabel pageSub =
        new JLabel(
            "Every account ever created, including inactive ones. Edit a row to change login / role / "
                + "password / profile, or delete it to move it to the trash bin.");
    pageSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
    pageSub.setForeground(new Color(110, 122, 140));
    JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
    titleBlock.setOpaque(false);
    titleBlock.add(pageTitle, BorderLayout.NORTH);
    titleBlock.add(pageSub, BorderLayout.SOUTH);
    scrollArea.add(titleBlock);

    accountsTable = new JTable(tableModel);
    accountsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
    accountsTable.setRowHeight(38);
    accountsTable.setGridColor(new Color(230, 230, 230));
    accountsTable.setSelectionBackground(new Color(227, 242, 253));
    accountsTable.setSelectionForeground(TEXT_DARK);

    JTableHeader tableHeader = accountsTable.getTableHeader();
    tableHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
    tableHeader.setBackground(WHITE);
    tableHeader.setForeground(TEXT_DARK);
    tableHeader.setPreferredSize(new Dimension(100, 40));

    JScrollPane tableScroll = new JScrollPane(accountsTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    tableScroll.getViewport().setBackground(WHITE);
    scrollArea.add(tableScroll);

    return scrollArea;
  }

  /** Re-reads all accounts (called on open / after any mutation). */
  public void loadAccountsData() {
    tableModel.setRowCount(0);
    List<User> users;
    try {
      users = userDAO.findAll();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    // Role names: query the roles table once into a Map<id, name>.
    Map<Integer, String> roleNames = new HashMap<>();
    for (Role role : new RoleDAO().findAll()) {
      roleNames.put(role.getRoleId(), role.getRoleName());
    }

    for (User user : users) {
      tableModel.addRow(
          new Object[] {
            user.getUsername(),
            roleNames.getOrDefault(user.getRoleId(), "--"),
            linkedName(user),
            user.isActive() ? "Active" : "Inactive",
            user.getCreatedAt() == null ? "--" : user.getCreatedAt().format(CREATED_FMT)
          });
      int r = tableModel.getRowCount() - 1;
      JButton edit = new JButton("Edit");
      styleSmallButton(edit, PRIMARY_BLUE);
      edit.addActionListener(e -> openEditDialog(user.getUserId()));
      tableModel.setValueAt(edit, r, 5);

      JButton del = new JButton("Delete");
      styleSmallButton(del, DANGER);
      del.addActionListener(e -> confirmDelete(user.getUserId()));
      tableModel.setValueAt(del, r, 6);
    }
  }

  /** Name from the linked patient/staff/doctor profile when the account has one, else "-". */
  private String linkedName(User user) {
    Patient patient = patientDAO.findByUserId(user.getUserId());
    if (patient != null) {
      return patient.getFirstName() + " " + patient.getLastName();
    }
    Doctor doctor = doctorDAO.findByUser(user.getUserId());
    if (doctor != null) {
      return "Dr. " + doctor.getFirstName() + " " + doctor.getLastName();
    }
    Staff staff = staffDAO.findByUser(user.getUserId());
    if (staff != null) {
      return staff.getFirstName() + " " + staff.getLastName();
    }
    return "-";
  }

  // ================= EDIT DIALOG =================

  private void openEditDialog(int userId) {
    User current = userDAO.findById(userId);
    if (current == null) {
      return;
    }
    Patient patient = patientDAO.findByUserId(userId);
    Doctor doctor = doctorDAO.findByUser(userId);
    Staff staff = staffDAO.findByUser(userId);

    JDialog dialog =
        new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Edit Account — " + current.getUsername(),
            true);
    JPanel content =
        new JPanel(
            new MigLayout(
                "insets 20 24, wrap 2, gapx 14, gapy 10", "[right][grow 220]"));
    content.setBackground(WHITE);

    JLabel heading =
        new JLabel("Edit Account — " + current.getUsername());
    heading.setFont(new Font("SansSerif", Font.BOLD, 16));
    heading.setForeground(TEXT_DARK);
    content.add(heading, "span 2");

    content.add(new JLabel("Username:"));
    JTextField usernameField = new JTextField(current.getUsername());
    content.add(usernameField);

    content.add(new JLabel("Role:"));
    JComboBox<String> roleCombo = new JComboBox<>();
    for (Role role : new RoleDAO().findAll()) {
      roleCombo.addItem(role.getRoleName());
    }
    int selIndex = -1;
    for (int i = 0; i < roleCombo.getItemCount(); i++) {
      if (roleCombo.getItemAt(i).equals(currentRoleName(current))) {
        selIndex = i;
        break;
      }
    }
    if (selIndex < 0) {
      selIndex = 0;
    }
    roleCombo.setSelectedIndex(selIndex);
    content.add(roleCombo);

    content.add(new JLabel("Active:"));
    JCheckBox activeCheck = new JCheckBox("", current.isActive());
    activeCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
    content.add(activeCheck);

    content.add(new JLabel("New Password:"));
    JPasswordField passwordField = new JPasswordField(20);
    content.add(passwordField);
    JLabel pwHint = new JLabel("(leave blank to keep current)");
    pwHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
    pwHint.setForeground(new Color(110, 122, 140));
    content.add(pwHint);

    // Linked-profile fields (final + null when the account has no such profile, so the OK
    // lambda below can reference them).
    final JTextField pFirst;
    final JTextField pLast;
    final JTextField pPhone;
    if (patient != null) {
      content.add(sectionLabel("Linked Patient Profile"), "span 2, gap 6 0");
      content.add(new JLabel("First Name:"));
      pFirst = new JTextField(patient.getFirstName());
      content.add(pFirst);
      content.add(new JLabel("Last Name:"));
      pLast = new JTextField(patient.getLastName());
      content.add(pLast);
      content.add(new JLabel("Phone:"));
      pPhone = new JTextField(patient.getPhone() == null ? "" : patient.getPhone());
      content.add(pPhone);
    } else {
      pFirst = pLast = pPhone = null;
    }

    final JTextField dFirst;
    final JTextField dLast;
    final JTextField dLicense;
    if (doctor != null) {
      content.add(sectionLabel("Linked Doctor Profile"), "span 2, gap 6 0");
      content.add(new JLabel("First Name:"));
      dFirst = new JTextField(doctor.getFirstName());
      content.add(dFirst);
      content.add(new JLabel("Last Name:"));
      dLast = new JTextField(doctor.getLastName());
      content.add(dLast);
      content.add(new JLabel("License No:"));
      dLicense = new JTextField(doctor.getLicenseNum() == null ? "" : doctor.getLicenseNum());
      content.add(dLicense);
    } else {
      dFirst = dLast = dLicense = null;
    }

    final JTextField sFirst;
    final JTextField sLast;
    final JTextField sPhone;
    if (staff != null) {
      content.add(sectionLabel("Linked Staff Profile"), "span 2, gap 6 0");
      content.add(new JLabel("First Name:"));
      sFirst = new JTextField(staff.getFirstName());
      content.add(sFirst);
      content.add(new JLabel("Last Name:"));
      sLast = new JTextField(staff.getLastName());
      content.add(sLast);
      content.add(new JLabel("Phone:"));
      sPhone = new JTextField(staff.getPhone() == null ? "" : staff.getPhone());
      content.add(sPhone);
    } else {
      sFirst = sLast = sPhone = null;
    }

    JButton okBtn = new JButton("OK");
    styleButton(okBtn, PRIMARY_BLUE);
    JButton cancelBtn = new JButton("Cancel");
    styleButton(cancelBtn, new Color(113, 128, 150));
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    buttons.setOpaque(false);
    buttons.add(okBtn);
    buttons.add(cancelBtn);
    content.add(buttons, "span 2, align right, gaptop 6");

    dialog.setContentPane(content);
    dialog.pack();
    dialog.setLocationRelativeTo(this);

    okBtn.addActionListener(
        e -> {
          String error =
              applyEdit(
                  current,
                  usernameField.getText().trim(),
                  roleCombo.getSelectedItem(),
                  activeCheck.isSelected(),
                  passwordField.getText(),
                  patient,
                  pFirst == null ? null : new String[] {
                    pFirst.getText().trim(), pLast.getText().trim(), pPhone.getText().trim()
                  },
                  doctor,
                  dFirst == null ? null : new String[] {
                    dFirst.getText().trim(), dLast.getText().trim(), dLicense.getText().trim()
                  },
                  staff,
                  sFirst == null ? null : new String[] {
                    sFirst.getText().trim(), sLast.getText().trim(), sPhone.getText().trim()
                  });
          if (error != null) {
            JOptionPane.showMessageDialog(
                dialog, error, "Invalid", JOptionPane.WARNING_MESSAGE);
          } else {
            dialog.dispose();
            loadAccountsData();
            JOptionPane.showMessageDialog(
                this, "Account updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
          }
        });
    cancelBtn.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
  }

  private JLabel sectionLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    label.setForeground(PRIMARY_BLUE);
    return label;
  }

  private String currentRoleName(User user) {
    Role role = new RoleDAO().findById(user.getRoleId());
    return role == null ? null : role.getRoleName();
  }

  private String applyEdit(
      User current,
      String newUsername,
      Object roleChoice,
      boolean active,
      String newPassword,
      Patient patient,
      String[] pVals,
      Doctor doctor,
      String[] dVals,
      Staff staff,
      String[] sVals) {
    if (newUsername == null || newUsername.isEmpty()) {
      return "Username is required.";
    }
    if (!newUsername.equals(current.getUsername())) {
      User other = userDAO.findByUsername(newUsername);
      if (other != null && other.getUserId() != current.getUserId()) {
        return "Username \"" + newUsername + "\" is already taken.";
      }
    }
    Role role = new RoleDAO().findByName(String.valueOf(roleChoice));
    if (role == null) {
      return "Role \"" + roleChoice + "\" not found.";
    }
    int roleId = role.getRoleId();
    try {
      User fresh = userDAO.findById(current.getUserId());
      fresh.setUsername(newUsername);
      fresh.setRoleId(roleId);
      fresh.setActive(active);
      if (newPassword != null && !newPassword.isEmpty()) {
        fresh.setPasswordHash(new AuthenticationService(userDAO).hashPassword(newPassword));
      }
      new UserController(new org.hospitalqueing.service.UserService(userDAO)).updateUser(fresh);

      if (patient != null && pVals != null && (pVals[0].isEmpty() || pVals[1].isEmpty())) {
        return "Patient first and last name are required.";
      }
      if (patient != null && pVals != null) {
        patient.setFirstName(pVals[0]);
        patient.setLastName(pVals[1]);
        patient.setPhone(pVals[2]);
        new PatientController(new org.hospitalqueing.service.PatientService(patientDAO)).updatePatient(patient);
      }
      if (doctor != null && dVals != null && (dVals[0].isEmpty() || dVals[1].isEmpty())) {
        return "Doctor first and last name are required.";
      }
      if (doctor != null && dVals != null) {
        doctor.setFirstName(dVals[0]);
        doctor.setLastName(dVals[1]);
        doctor.setLicenseNum(dVals[2]);
        new DoctorController(new org.hospitalqueing.service.DoctorService(doctorDAO)).updateDoctor(doctor);
      }
      if (staff != null && sVals != null && (sVals[0].isEmpty() || sVals[1].isEmpty())) {
        return "Staff first and last name are required.";
      }
      if (staff != null && sVals != null) {
        staff.setFirstName(sVals[0]);
        staff.setLastName(sVals[1]);
        staff.setPhone(sVals[2]);
        new StaffController(new org.hospitalqueing.service.StaffService(staffDAO)).updateStaff(staff);
      }
      return null;
    } catch (Exception ex) {
      ex.printStackTrace();
      return "Could not save changes: " + ex.getMessage();
    }
  }

  // ================= DELETE (SOFT, TO TRASH BIN) =================

  private void confirmDelete(int userId) {
    User current = userDAO.findById(userId);
    if (current == null) {
      return;
    }
    if (parentFrame != null
        && parentFrame.getLoggedInUser() != null
        && current.getUsername().equals(parentFrame.getLoggedInUser().getUsername())) {
      JOptionPane.showMessageDialog(
          this,
          "You cannot delete the account you are currently logged in with.",
          "Delete account",
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Move account \"" + current.getUsername() + "\" to the trash bin?\n"
                + "It can be restored from the Trash Bin.",
            "Delete account",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      userDAO.softDelete(userId);
      // Trash the linked patient row with the account (if it has one).
      Patient patient = patientDAO.findByUserId(userId);
      if (patient != null) {
        patientDAO.softDelete(patient.getPatientId());
      }
      loadAccountsData();
      JOptionPane.showMessageDialog(
          this, "Account moved to the trash bin.", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      ex.printStackTrace();
      loadAccountsData();
      JOptionPane.showMessageDialog(
          this, "Could not delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ================= STYLING + SMOKE-TEST ACCESSORS =================

  private void styleButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 13));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setPreferredSize(new Dimension(120, 34));
  }

  private void styleSmallButton(JButton b, Color c) {
    b.setBackground(c);
    b.setForeground(WHITE);
    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setFont(new Font("SansSerif", Font.BOLD, 11));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public JTable getAccountsTable() {
    return accountsTable;
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }
}
