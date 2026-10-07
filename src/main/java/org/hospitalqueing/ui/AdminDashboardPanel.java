package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.model.Appointment;

/**
 * The admin dashboard: same shell as the staff dashboard (dark header + content cards) but with
 * admin-only sections — Departments, Services, Doctors, Staff — plus live facility stats.
 * Regular staff keep the StaffDashboardPanel; login routes by role.
 */
public class AdminDashboardPanel extends JPanel {

  private final Color HEADER_DARK_BLUE = new Color(13, 37, 63);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color WHITE = Color.WHITE;
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);

  private final MainFrame parentFrame;
  private final CardLayout adminCardLayout = new CardLayout();
  private final JPanel adminContentPanel;

  private final AdminDepartmentsPanel departmentsPanel;
  private final AdminServicesPanel servicesPanel;
  private final AdminDoctorsPanel doctorsPanel;
  private final AdminStaffPanel staffPanel;
  private JPanel homeScreen;

  public AdminDashboardPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    // --- 1. ADMIN HEADER (Dark Blue Nav) ---
    JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]push[center]25[center]25[center]25[center]push[right]", "[center]"));
    headerPanel.setBackground(HEADER_DARK_BLUE);
    headerPanel.add(new JLabel(" "), "cell 0 0");

    JLabel homeNav = createHeaderLink("DASHBOARD", true);
    JLabel deptNav = createHeaderLink("DEPARTMENTS", false);
    JLabel serviceNav = createHeaderLink("SERVICES", false);
    JLabel doctorNav = createHeaderLink("DOCTORS", false);
    JLabel staffNav = createHeaderLink("STAFF", false);

    headerPanel.add(homeNav, "cell 1 0");
    headerPanel.add(deptNav, "cell 2 0");
    headerPanel.add(serviceNav, "cell 3 0");
    headerPanel.add(doctorNav, "cell 4 0");
    headerPanel.add(staffNav, "cell 5 0");

    JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    rightControls.setOpaque(false);
    JButton logoutBtn = new JButton("Logout");
    logoutBtn.setBackground(WHITE);
    logoutBtn.setForeground(HEADER_DARK_BLUE);
    logoutBtn.setFocusPainted(false);
    logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
    logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    rightControls.add(logoutBtn);
    headerPanel.add(rightControls, "cell 6 0");

    add(headerPanel, BorderLayout.NORTH);

    // --- 2. ADMIN CONTENT (internal CardLayout) ---
    adminContentPanel = new JPanel(adminCardLayout);
    adminContentPanel.setBackground(BACKGROUND_LIGHT);

    departmentsPanel = new AdminDepartmentsPanel(parentFrame);
    servicesPanel = new AdminServicesPanel(parentFrame);
    doctorsPanel = new AdminDoctorsPanel(parentFrame);
    staffPanel = new AdminStaffPanel(parentFrame);

    homeScreen = buildHomeScreen();
    adminContentPanel.add(homeScreen, "ADMIN_HOME");
    adminContentPanel.add(departmentsPanel, "ADMIN_DEPARTMENTS");
    adminContentPanel.add(servicesPanel, "ADMIN_SERVICES");
    adminContentPanel.add(doctorsPanel, "ADMIN_DOCTORS");
    adminContentPanel.add(staffPanel, "ADMIN_STAFF");

    add(adminContentPanel, BorderLayout.CENTER);

    // --- 3. NAV WIRING ---
    homeNav.addMouseListener(onClick(() -> showAdmin("ADMIN_HOME")));
    deptNav.addMouseListener(onClick(() -> showAdmin("ADMIN_DEPARTMENTS")));
    serviceNav.addMouseListener(onClick(() -> showAdmin("ADMIN_SERVICES")));
    doctorNav.addMouseListener(onClick(() -> showAdmin("ADMIN_DOCTORS")));
    staffNav.addMouseListener(onClick(() -> showAdmin("ADMIN_STAFF")));

    logoutBtn.addActionListener(e -> {
      int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION) {
        parentFrame.triggerLogout();
      }
    });
  }

  /** Switch admin section (called from header nav). The home section rebuilds so its stats are live. */
  public void showAdmin(String card) {
    if ("ADMIN_HOME".equals(card) && homeScreen != null) {
      adminContentPanel.remove(homeScreen);
      homeScreen = buildHomeScreen();
      adminContentPanel.add(homeScreen, "ADMIN_HOME");
      revalidate();
      repaint();
    }
    adminCardLayout.show(adminContentPanel, card);
  }

  /** Re-queries all admin data; called by MainFrame when the app returns to the logged-in home. */
  public void refreshAll() {
    departmentsPanel.refresh();
    servicesPanel.refresh();
    doctorsPanel.refresh();
    staffPanel.refresh();
  }

  private java.awt.event.MouseListener onClick(Runnable action) {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        action.run();
      }
    };
  }

  // --- HOME SCREEN ---
  private JPanel buildHomeScreen() {
    JPanel panel = new JPanel(new MigLayout("wrap 1, insets 30 40 30 40, fillx", "[grow, fill]", "[]30[]30[]"));
    panel.setBackground(BACKGROUND_LIGHT);

    // A. Greeting — the actually logged-in admin account.
    org.hospitalqueing.model.User adminUser = parentFrame != null ? parentFrame.getLoggedInUser() : null;
    String adminName = (adminUser != null && adminUser.getUsername() != null) ? adminUser.getUsername() : "Admin";
    String adminRole = UiData.roleNameForUser(adminUser);
    String roleDisplay = (adminRole != null && !adminRole.isBlank()) ? adminRole + " • Facility Management" : "Admin • Facility Management";

    JPanel greetingPanel = new JPanel(new MigLayout("insets 0", "[left]", "[]2[]"));
    greetingPanel.setOpaque(false);
    JLabel greetingText = new JLabel("Good day,");
    greetingText.setFont(new Font("SansSerif", Font.PLAIN, 16));
    greetingText.setForeground(TEXT_MUTED);
    JLabel adminNameLbl = new JLabel(adminName);
    adminNameLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
    adminNameLbl.setForeground(TEXT_DARK);
    JLabel adminRoleLbl = new JLabel(roleDisplay);
    adminRoleLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
    adminRoleLbl.setForeground(TEXT_MUTED);
    greetingPanel.add(greetingText, "wrap");
    greetingPanel.add(adminNameLbl, "wrap");
    greetingPanel.add(adminRoleLbl);
    panel.add(greetingPanel);

    // B. Live facility stats.
    JPanel summaryContainer = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill][grow, fill]", "[]"));
    summaryContainer.setOpaque(false);
    summaryContainer.add(createStatCard("Departments", String.valueOf(countSafe(() -> new DepartmentDAO().findAll().size())), "🏥", new Color(230, 244, 255), PRIMARY_BLUE));
    summaryContainer.add(createStatCard("Doctors", String.valueOf(countSafe(() -> new DoctorDAO().findAll().size())), "🩺", new Color(235, 249, 241), new Color(46, 204, 113)));
    summaryContainer.add(createStatCard("Staff Accounts", String.valueOf(countSafe(() -> new StaffDAO().findAll().size())), "🧑‍⚕️", new Color(255, 244, 229), new Color(230, 126, 34)));
    summaryContainer.add(createStatCard("Open Appointments", String.valueOf(countOpenAppointments()), "📅", new Color(240, 235, 255), new Color(123, 92, 227)));
    panel.add(summaryContainer);

    // C. Management quick actions.
    JLabel actionsLabel = new JLabel("Manage");
    actionsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    actionsLabel.setForeground(TEXT_DARK);
    panel.add(actionsLabel, "gaptop 10");

    JPanel actionsContainer = new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill][grow, fill]", "[]"));
    actionsContainer.setOpaque(false);
    actionsContainer.add(createActionCard("Departments", "Add or remove hospital units", "🏥", "ADMIN_DEPARTMENTS"));
    actionsContainer.add(createActionCard("Services", "What patients can book", "🩻", "ADMIN_SERVICES"));
    actionsContainer.add(createActionCard("Doctors", "Who is on roster", "🩺", "ADMIN_DOCTORS"));
    actionsContainer.add(createActionCard("Staff", "Logins & roles", "🧑‍⚕️", "ADMIN_STAFF"));
    panel.add(actionsContainer);

    return panel;
  }

  private int countOpenAppointments() {
    try {
      int count = 0;
      for (Appointment a : new AppointmentDAO().findAll()) {
        if ("SCHEDULED".equalsIgnoreCase(a.getStatus()) || "CONFIRMED".equalsIgnoreCase(a.getStatus())) {
          count++;
        }
      }
      return count;
    } catch (Exception ex) {
      return 0;
    }
  }

  private int countSafe(java.util.function.IntSupplier supplier) {
    try {
      return supplier.getAsInt();
    } catch (Exception ex) {
      return 0;
    }
  }

  // --- UI HELPERS (same style language as StaffDashboardPanel) ---
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
        if (targetScreen != null && !targetScreen.isEmpty()) {
          showAdmin(targetScreen);
        }
      }
    });
    return card;
  }
}
