package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueStatus;
import org.hospitalqueing.model.Staff;
import org.hospitalqueing.model.User;
import org.hospitalqueing.service.QueueManagementService;

public class StaffDashboardPanel extends JPanel {

    // Colors matching your design
    private final Color HEADER_DARK_BLUE = new Color(13, 37, 63); 
    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color TEXT_MUTED = new Color(113, 128, 150);

    private CardLayout staffCardLayout;
    private JPanel staffContentPanel;
    private StaffPatientQueuePanel staffPatientPanel;
    private MainFrame parentFrame;

    // Live stat count labels, updated each time the home screen is shown (the panel is
    // constructed before login, so buildHomeScreen() may not yet know the logged-in staff).
    private JLabel todayPatientsLabel;
    private JLabel inQueueLabel;
    private JLabel completedLabel;

    public StaffDashboardPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_LIGHT);
        
        // --- 1. STAFF HEADER (Dark Blue Nav) ---
        JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]push[center]30[center]30[center]push[right]", "[center]"));
        headerPanel.setBackground(HEADER_DARK_BLUE);

        // Blank placeholder space on the left to balance the center alignment
        headerPanel.add(new JLabel(" "), "cell 0 0");

        // Center Navigation Links
        JLabel homeNav = createHeaderLink("HOME", true);
        JLabel walkInNav = createHeaderLink("WALK-IN", false);
        JLabel patientNav = createHeaderLink("PATIENT", false);

        headerPanel.add(homeNav, "cell 1 0");
        headerPanel.add(walkInNav, "cell 2 0");
        headerPanel.add(patientNav, "cell 3 0");

        // Right side icons (Notification & Profile)
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);
        
        JLabel notifIcon = new JLabel("🔔");
        notifIcon.setForeground(WHITE);
        notifIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        notifIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel profileIcon = new JLabel("👤");
        profileIcon.setForeground(WHITE);
        profileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        profileIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(WHITE);
        logoutBtn.setForeground(HEADER_DARK_BLUE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rightControls.add(notifIcon);
        rightControls.add(profileIcon);
        rightControls.add(logoutBtn);

        headerPanel.add(rightControls, "cell 4 0");

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. STAFF CONTENT AREA (Internal CardLayout) ---
        staffCardLayout = new CardLayout();
        staffContentPanel = new JPanel(staffCardLayout);
        staffContentPanel.setBackground(BACKGROUND_LIGHT);

        // Create the individual staff screens
        JPanel homeScreen = buildHomeScreen();
        JPanel walkInScreen = new StaffWalkInPanel();
        staffPatientPanel = new StaffPatientQueuePanel();
        JPanel patientScreen = staffPatientPanel;

        staffContentPanel.add(homeScreen, "STAFF_HOME");
        staffContentPanel.add(walkInScreen, "STAFF_WALKIN");
        staffContentPanel.add(patientScreen, "STAFF_PATIENT");

        // Re-compute the live stat cards whenever the home screen becomes visible, so the
        // numbers always reflect the currently logged-in staff and the latest queue state
        // (the panel is constructed before login, and queue state changes after login).
        homeScreen.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshHomeStats();
            }
        });

        // Re-query the live queue whenever the patient screen becomes visible, so the operator
        // always sees current data no matter how they navigated here (nav link, quick-action card,
        // or a future route). componentShown() fires when this panel transitions to showing,
        // which is exactly when a CardLayout page is selected.
        patientScreen.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                staffPatientPanel.refresh();
            }
        });

        add(staffContentPanel, BorderLayout.CENTER);

        // --- 3. ACTIONS ---
        homeNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showStaffHome(); }
        });
        walkInNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { staffCardLayout.show(staffContentPanel, "STAFF_WALKIN"); }
        });
        patientNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showStaffPatient(); }
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                parentFrame.triggerLogout();
            }
        });
    }

    /** Shows the live patient queue screen and refreshes it. */
    private void showStaffPatient() {
        staffCardLayout.show(staffContentPanel, "STAFF_PATIENT");
        if (staffPatientPanel != null) {
            staffPatientPanel.refresh();
        }
    }

    /** Shows the home screen and re-computes the live stat cards. */
    private void showStaffHome() {
        staffCardLayout.show(staffContentPanel, "STAFF_HOME");
        refreshHomeStats();
    }

    /** Re-computes and updates the three live stat cards for the currently logged-in staff. */
    private void refreshHomeStats() {
        org.hospitalqueing.model.User staffUser =
                parentFrame != null ? parentFrame.getLoggedInUser() : null;
        if (todayPatientsLabel != null) {
            todayPatientsLabel.setText(String.valueOf(countSafe(() -> countToday(staffUser))));
        }
        if (inQueueLabel != null) {
            inQueueLabel.setText(String.valueOf(countSafe(() -> countInQueue(staffUser))));
        }
        if (completedLabel != null) {
            completedLabel.setText(String.valueOf(countSafe(() -> countCompleted(staffUser))));
        }
    }

    // --- HOME SCREEN BUILDER ---
    private JPanel buildHomeScreen() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 30 40 30 40, fillx", "[grow, fill]", "[]30[]30[]"));
        panel.setBackground(BACKGROUND_LIGHT);

        // A. Greeting Section — reflects the actually logged-in staff account.
        org.hospitalqueing.model.User staffUser =
                parentFrame != null ? parentFrame.getLoggedInUser() : null;
        String staffName = (staffUser != null && staffUser.getUsername() != null)
                ? staffUser.getUsername() : "Staff";
        String staffRole = UiData.roleNameForUser(staffUser);
        String roleDisplay = (staffRole != null && !staffRole.isBlank())
                ? staffRole + " • On duty" : "Staff • On duty";

        JPanel greetingPanel = new JPanel(new MigLayout("insets 0", "[left]", "[]2[]"));
        greetingPanel.setOpaque(false);
        JLabel greetingText = new JLabel("Good day,");
        greetingText.setFont(new Font("SansSerif", Font.PLAIN, 16));
        greetingText.setForeground(TEXT_MUTED);

        JLabel docName = new JLabel(staffName);
        docName.setFont(new Font("SansSerif", Font.BOLD, 24));
        docName.setForeground(TEXT_DARK);

        JLabel docRole = new JLabel(roleDisplay);
        docRole.setFont(new Font("SansSerif", Font.PLAIN, 14));
        docRole.setForeground(TEXT_MUTED);

        greetingPanel.add(greetingText, "wrap");
        greetingPanel.add(docName, "wrap");
        greetingPanel.add(docRole);
        panel.add(greetingPanel);

        // B. Summary Cards Section — live counts for the logged-in staff member's department
        // (falls back to totals across departments when the staff row has no department).
        JPanel summaryContainer = new JPanel(new MigLayout("insets 0, gap 20", "[grow, fill][grow, fill][grow, fill]", "[]"));
        summaryContainer.setOpaque(false);

        int todayPatients = countSafe(() -> countToday(staffUser));
        int inQueue = countSafe(() -> countInQueue(staffUser));
        int completed = countSafe(() -> countCompleted(staffUser));

        todayPatientsLabel = createCountLabel(String.valueOf(todayPatients));
        inQueueLabel = createCountLabel(String.valueOf(inQueue));
        completedLabel = createCountLabel(String.valueOf(completed));

        summaryContainer.add(createStatCard("Today's Patients", todayPatientsLabel, "👥", new Color(230, 244, 255), PRIMARY_BLUE));
        summaryContainer.add(createStatCard("In Queue", inQueueLabel, "🕒", new Color(255, 244, 229), new Color(230, 126, 34)));
        summaryContainer.add(createStatCard("Completed", completedLabel, "✅", new Color(235, 249, 241), new Color(46, 204, 113)));

        panel.add(summaryContainer);

        // C. Quick Actions Section
        JLabel actionsLabel = new JLabel("Quick Actions");
        actionsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        actionsLabel.setForeground(TEXT_DARK);
        panel.add(actionsLabel, "gaptop 10");

        JPanel actionsContainer = new JPanel(new MigLayout("insets 0, gap 20", "[grow, fill][grow, fill][grow, fill]", "[]"));
        actionsContainer.setOpaque(false);

        // THE FIX: We pass the exact target screen string to the card builder so it actually navigates!
        actionsContainer.add(createActionCard("View Queue", "Manage current patient queue", "📋", "STAFF_PATIENT"));
        actionsContainer.add(createActionCard("Register Walk-In", "Add patient to queue", "📝", "STAFF_WALKIN"));
        actionsContainer.add(createActionCard("Appointments", "View today's schedule", "📅", "")); // Left blank because we don't have a screen for this yet

        panel.add(actionsContainer);

        return panel;
    }

    // --- LIVE STATS HELPERS (scoped to the staff member's department; all-dept totals as fallback) ---

    /** The logged-in staff row's department, or null when there is no staff row / no department. */
    private Integer staffDepartmentId(User staffUser) {
        if (staffUser == null) {
            return null;
        }
        Staff staff = new StaffDAO().findByUser(staffUser.getUserId());
        return (staff != null) ? staff.getDepartmentId() : null;
    }

    /** Today's date string, in the same shape queue_entries.queue_date is stored (ISO yyyy-MM-dd). */
    private static String today() {
        return LocalDate.now().toString();
    }

    /** queue_entries rows for today: the staff dept only, or all depts when the staff has none. */
    private List<QueueEntry> todaysEntries(User staffUser) {
        Integer deptId = staffDepartmentId(staffUser);
        String date = today();
        return new QueueEntryDAO().findAll().stream()
            .filter(e -> e.getQueueDate() != null && e.getQueueDate().equals(date))
            .filter(e -> deptId == null || e.getDepartmentId() == deptId)
            .toList();
    }

    /** "Today's Patients": every queue entry created today in the staff's department. */
    private int countToday(User staffUser) {
        return todaysEntries(staffUser).size();
    }

    /** "Completed": today's entries whose status is terminal (Discharged / Completed / No Show). */
    private int countCompleted(User staffUser) {
        int count = 0;
        for (QueueEntry e : todaysEntries(staffUser)) {
            if (QueueStatus.isTerminal(e.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /** "In Queue": size of the live active queue. The service is per-department, so a staff
     *  member without a department is shown the all-department active total instead. */
    private int countInQueue(User staffUser) {
        QueueEntryDAO queueEntryDAO = new QueueEntryDAO();
        QueueManagementService qms =
            new QueueManagementService(
                queueEntryDAO,
                new QueueEventDAO(),
                new ServiceDAO(),
                new DepartmentDAO(),
                new CounterDAO());
        Integer deptId = staffDepartmentId(staffUser);
        if (deptId != null) {
            return qms.getActiveQueue(deptId).size();
        }
        // Fallback: active entries across departments.
        return (int) queueEntryDAO.findAll().stream().filter(e -> QueueStatus.isActive(e.getStatus())).count();
    }

    private int countSafe(java.util.function.IntSupplier supplier) {
        try {
            return supplier.getAsInt();
        } catch (Exception ex) {
            return 0;
        }
    }

    // --- UI HELPERS ---
    private JLabel createHeaderLink(String text, boolean isActive) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(isActive ? WHITE : new Color(150, 170, 190));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { label.setForeground(WHITE); }
            public void mouseExited(MouseEvent e) { if(!isActive) label.setForeground(new Color(150, 170, 190)); }
        });
        return label;
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

        card.add(titleLbl, "cell 0 0");
        card.add(iconLbl, "cell 1 0");
        card.add(countLbl, "cell 0 1, span 2");

        return card;
    }

    private JLabel createCountLabel(String count) {
        JLabel countLbl = new JLabel(count);
        countLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        countLbl.setForeground(TEXT_DARK);
        return countLbl;
    }

    // UPDATED: Now accepts a targetScreen parameter for real navigation
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

        // Hover Effect & Click Action
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 249, 255)); }
            public void mouseExited(MouseEvent e) { card.setBackground(WHITE); }
            public void mouseClicked(MouseEvent e) {
                if (targetScreen != null && !targetScreen.isEmpty()) {
                    staffCardLayout.show(staffContentPanel, targetScreen);
                } else {
                    JOptionPane.showMessageDialog(card, "Opening " + title + " Module (In Development)...");
                }
            }
        });

        return card;
    }
}