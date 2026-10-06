package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PatientDashboardPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);   
    private final Color LIGHT_BLUE = new Color(227, 242, 253);    
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JButton appointmentBtn;
    private JButton queueBtn;
    private JButton logoutBtn;
    
    private JScrollPane scrollPane;
    private JPanel scrollContentPanel;

    public PatientDashboardPanel(MainFrame parentFrame) {
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- 1. LOGGED-IN NAVBAR ---
        JPanel loggedInNav = new JPanel(new MigLayout("insets 15 20 15 20, aligny center", "[left]push[center]10[center]10[center]10[right]", "[center]"));
        loggedInNav.setBackground(WHITE);

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("✚"); 
        logoLabel.setForeground(PRIMARY_BLUE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel titleLabel = new JLabel("HOSPITAL");
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        brandPanel.add(logoLabel);
        brandPanel.add(titleLabel);

        JLabel homeNav = createNavLink("Home");
        JLabel aboutNav = createNavLink("About");
        JLabel servicesNav = createNavLink("Services");
        JLabel historyNav = createNavLink("History");

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linksPanel.setOpaque(false);
        linksPanel.add(homeNav);
        linksPanel.add(aboutNav);
        linksPanel.add(servicesNav);
        linksPanel.add(historyNav);

        JButton notifIconBtn = createIconButton("🔔");
        JButton profileIconBtn = createIconButton("👤");
        
        logoutBtn = new JButton("Logout");
        styleSecondaryButton(logoutBtn);
        logoutBtn.setPreferredSize(new Dimension(80, 30));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightControls.setOpaque(false);
        rightControls.add(notifIconBtn);
        rightControls.add(profileIconBtn);
        rightControls.add(logoutBtn);

        loggedInNav.add(brandPanel, "cell 0 0");
        loggedInNav.add(linksPanel, "cell 1 0");
        loggedInNav.add(rightControls, "cell 4 0");

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(loggedInNav, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(230, 230, 230));
        topContainer.add(separator, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // --- 2. SCROLLABLE CONTENT AREA ---
        scrollContentPanel = new JPanel(new MigLayout("wrap, fillx, insets 15 20 20 20", "[grow, fill]", "")) {
            @Override
            public void scrollRectToVisible(Rectangle aRect) { }
        };
        scrollContentPanel.setBackground(WHITE);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonPanel.setOpaque(false);

        appointmentBtn = new JButton("Appointment");
        queueBtn = new JButton("Queue");
        
        styleActionButton(appointmentBtn);
        styleActionButton(queueBtn);

        actionButtonPanel.add(appointmentBtn);
        actionButtonPanel.add(queueBtn);
        scrollContentPanel.add(actionButtonPanel, "align center, gapbottom 15");

        JPanel bannerPanel = new JPanel(new GridBagLayout());
        bannerPanel.setPreferredSize(new Dimension(740, 135)); 
        bannerPanel.setBackground(LIGHT_BLUE);
        bannerPanel.setBorder(BorderFactory.createLineBorder(new Color(187, 222, 251), 1, true));
        
        JLabel bannerText = new JLabel("Welcome Banner");
        bannerText.setFont(new Font("SansSerif", Font.BOLD, 26));
        bannerText.setForeground(PRIMARY_BLUE);
        bannerPanel.add(bannerText);
        
        scrollContentPanel.add(bannerPanel, "growx, gapbottom 15");

        JLabel sectionTitle = new JLabel("Select Hospital Service or Department");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        sectionTitle.setForeground(TEXT_DARK);
        scrollContentPanel.add(sectionTitle, "gapbottom 10");

        JPanel deptGrid = new JPanel(new MigLayout("wrap 2, fill, gap 12 12", 
            "[0:0, grow, fill][0:0, grow, fill]", ""));
        deptGrid.setOpaque(false);

        deptGrid.add(createDepartmentCard("🚑", "Emergency Care", "24/7 Trauma & Urgent Medical Services", parentFrame));
        deptGrid.add(createDepartmentCard("♥", "Cardiology", "Heart Specialists, ECG & Diagnostics", parentFrame)); 
        deptGrid.add(createDepartmentCard("👶", "Pediatrics", "Child Care, Immunization & Wellness", parentFrame));
        deptGrid.add(createDepartmentCard("💉", "General Surgery", "Outpatient & Specialized Procedures", parentFrame));
        deptGrid.add(createDepartmentCard("🔬", "Radiology", "X-Ray, CT Scan, MRI & Ultrasound Lab", parentFrame));
        deptGrid.add(createDepartmentCard("💊", "Pharmacy", "Fast-Track Prescription & Medicine Queue", parentFrame));

        scrollContentPanel.add(deptGrid, "growx");

        scrollPane = new JScrollPane(scrollContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
        });

        // --- 3. WIRING ACTIONS ---
        notifIconBtn.addActionListener(e -> {
            JPopupMenu notifMenu = new JPopupMenu();
            notifMenu.setBackground(WHITE);
            JMenuItem headerItem = new JMenuItem("Notifications");
            headerItem.setFont(new Font("SansSerif", Font.BOLD, 12));
            headerItem.setEnabled(false); 
            JMenuItem notif1 = new JMenuItem("Your queue A-024 is now being called.");
            notif1.setBackground(new Color(235, 249, 241)); 
            JMenuItem notif2 = new JMenuItem("Your appointment is scheduled for tomorrow.");
            notifMenu.add(headerItem);
            notifMenu.addSeparator();
            notifMenu.add(notif1);
            notifMenu.add(notif2);
            notifMenu.show(notifIconBtn, 0, notifIconBtn.getHeight());
        });

        historyNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { parentFrame.showScreen("HISTORY_PAGE"); }
        });

        profileIconBtn.addActionListener(e -> {
            parentFrame.showScreen("PROFILE_PAGE");
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) { parentFrame.triggerLogout(); }
        });

        appointmentBtn.addActionListener(e -> { parentFrame.showScreen("APPOINTMENT_PAGE"); });
        queueBtn.addActionListener(e -> { parentFrame.showScreen("QUEUE_STATUS_PAGE"); });
    }

    private JPanel createDepartmentCard(String iconSymbol, String title, String description, MainFrame parentFrame) {
        JPanel card = new JPanel(new MigLayout("wrap 2, insets 20 14 20 14", "[left]10[grow, fill]", "[]4[]"));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 236), 1, true));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(iconSymbol);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26)); 

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLbl.setForeground(PRIMARY_BLUE);

        JLabel descLbl = new JLabel("<html><body style='width: 210px'>" + description + "</body></html>");
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLbl.setForeground(new Color(100, 116, 139));

        card.add(iconLbl, "spany 2, aligny top");
        card.add(titleLbl, "wrap");
        card.add(descLbl);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(LIGHT_BLUE); }
            public void mouseExited(MouseEvent e) { card.setBackground(new Color(248, 250, 252)); }
            // THE FIX: This now passes the card's title to the MainFrame to preselect the combo box!
            public void mouseClicked(MouseEvent e) { parentFrame.routeToQueueWithDepartment(title); } 
        });

        return card;
    }

    private JLabel createNavLink(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_DARK);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent e) { label.setForeground(PRIMARY_BLUE); }
            public void mouseExited(MouseEvent e) { label.setForeground(TEXT_DARK); }
        });
        return label;
    }

    private JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btn.setPreferredSize(new Dimension(36, 30));
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setBorder(null);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleActionButton(JButton button) {
        button.setPreferredSize(new Dimension(180, 38));
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(WHITE);
        button.setForeground(PRIMARY_BLUE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}