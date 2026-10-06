package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import org.hospitalqueing.service.AuthenticationService;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel registerLink;
    private JLabel backBtn;

    public LoginPanel(MainFrame parentFrame) {
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- 1. HEADER ---
        JPanel headerPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]10[left]push", "[center]"));
        headerPanel.setBackground(WHITE);

        backBtn = new JLabel("⬅");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        backBtn.setForeground(TEXT_DARK);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel logoLabel = new JLabel("✚");
        logoLabel.setForeground(PRIMARY_BLUE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JLabel titleLabel = new JLabel("HOSPITAL");
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        headerPanel.add(backBtn);
        headerPanel.add(logoLabel);
        headerPanel.add(titleLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(new JSeparator(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. LOGIN FORM CARD ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(240, 244, 248));

        JPanel card = new JPanel(new MigLayout("wrap 1, insets 40 50 40 50, center", "[center]", "[]10[]30[]15[]20[]20[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel loginTitle = new JLabel("WELCOME BACK");
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        loginTitle.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Log in to access your hospital portal.");
        subtitle.setForeground(new Color(113, 128, 150));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        String fieldConstraints = "width 280:300:300, height 38!";

        usernameField = new JTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Username");
        
        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Password");

        loginBtn = new JButton("Log In");
        loginBtn.setPreferredSize(new Dimension(300, 40));
        loginBtn.setBackground(PRIMARY_BLUE);
        loginBtn.setForeground(WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        registerLink = new JLabel("<html><u>Don't have an account? Register</u></html>");
        registerLink.setForeground(PRIMARY_BLUE);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(loginTitle);
        card.add(subtitle, "gapbottom 10");
        card.add(usernameField, fieldConstraints);
        card.add(passwordField, fieldConstraints);
        card.add(loginBtn, "gaptop 10");
        card.add(registerLink);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("LANDING_PAGE");
            }
        });

        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("REGISTER_PAGE");
            }
        });

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- HARDCODED STAFF BYPASS FOR UI TESTING ---
            if (username.equals("staff") && password.equals("admin")) {
                org.hospitalqueing.model.User dummyStaff = new org.hospitalqueing.model.User();
                dummyStaff.setUserId(999); // Dummy ID
                dummyStaff.setUsername("Dr. Tester");
                int staffRoleId = org.hospitalqueing.ui.UiData.roleIdByName("STAFF");
                dummyStaff.setRoleId(staffRoleId > 0 ? staffRoleId : 2); // 2 = STAFF in the seed
                parentFrame.setLoggedInUser(dummyStaff);
                clearFields();
                JOptionPane.showMessageDialog(this, "Test Login Successful! Welcome to the Staff Portal.");
                parentFrame.showScreen("ADMIN_DASHBOARD");
                return; // Stop here so it doesn't try to query the database
            }
            // ---------------------------------------------

            try {
                UserDAO userDAO = new UserDAO();
                AuthenticationService authService = new AuthenticationService(userDAO);
                User loggedInUser = authService.login(username, password);

                if (loggedInUser != null) {
                    parentFrame.setLoggedInUser(loggedInUser);
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Login Successful! Welcome back, " + loggedInUser.getUsername());

                    // Route by role name, not by hardcoded id (PATIENT/STAFF/ADMIN are seeded).
                    if (org.hospitalqueing.ui.UiData.isStaff(loggedInUser)) {
                        parentFrame.showScreen("ADMIN_DASHBOARD");
                    } else {
                        parentFrame.showScreen("DASHBOARD_PAGE");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}