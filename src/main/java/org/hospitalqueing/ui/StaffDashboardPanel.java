package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private MainFrame parentFrame;

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
        JPanel patientScreen = new StaffPatientQueuePanel();

        staffContentPanel.add(homeScreen, "STAFF_HOME");
        staffContentPanel.add(walkInScreen, "STAFF_WALKIN");
        staffContentPanel.add(patientScreen, "STAFF_PATIENT");

        add(staffContentPanel, BorderLayout.CENTER);

        // --- 3. ACTIONS ---
        homeNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { staffCardLayout.show(staffContentPanel, "STAFF_HOME"); }
        });
        walkInNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { staffCardLayout.show(staffContentPanel, "STAFF_WALKIN"); }
        });
        patientNav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { staffCardLayout.show(staffContentPanel, "STAFF_PATIENT"); }
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                parentFrame.triggerLogout();
            }
        });
    }

    // --- HOME SCREEN BUILDER ---
    private JPanel buildHomeScreen() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 30 40 30 40, fillx", "[grow, fill]", "[]30[]30[]"));
        panel.setBackground(BACKGROUND_LIGHT);

        // A. Greeting Section
        JPanel greetingPanel = new JPanel(new MigLayout("insets 0", "[left]", "[]2[]"));
        greetingPanel.setOpaque(false);
        JLabel greetingText = new JLabel("Good Morning,");
        greetingText.setFont(new Font("SansSerif", Font.PLAIN, 16));
        greetingText.setForeground(TEXT_MUTED);
        
        JLabel docName = new JLabel("Dr. Maria Santos"); 
        docName.setFont(new Font("SansSerif", Font.BOLD, 24));
        docName.setForeground(TEXT_DARK);

        JLabel docRole = new JLabel("Internal Medicine • Physician");
        docRole.setFont(new Font("SansSerif", Font.PLAIN, 14));
        docRole.setForeground(TEXT_MUTED);

        greetingPanel.add(greetingText, "wrap");
        greetingPanel.add(docName, "wrap");
        greetingPanel.add(docRole);
        panel.add(greetingPanel);

        // B. Summary Cards Section
        JPanel summaryContainer = new JPanel(new MigLayout("insets 0, gap 20", "[grow, fill][grow, fill][grow, fill]", "[]"));
        summaryContainer.setOpaque(false);

        summaryContainer.add(createStatCard("Today's Patients", "48", "👥", new Color(230, 244, 255), PRIMARY_BLUE));
        summaryContainer.add(createStatCard("In Queue", "12", "🕒", new Color(255, 244, 229), new Color(230, 126, 34)));
        summaryContainer.add(createStatCard("Completed", "36", "✅", new Color(235, 249, 241), new Color(46, 204, 113)));

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