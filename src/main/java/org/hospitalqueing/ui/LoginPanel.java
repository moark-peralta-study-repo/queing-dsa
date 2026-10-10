package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import org.hospitalqueing.service.AuthenticationService;
import org.hospitalqueing.dao.RoleDAO;
import org.hospitalqueing.dao.SecurityLogDAO;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.database.DatabaseConnection;
import org.hospitalqueing.model.Role;
import org.hospitalqueing.model.SecurityLog;
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
    private JCheckBox rememberMe;
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

        // T7: restore a remembered username (if any) up front. readUsername() returns null for a
        // missing, empty, or corrupt file, in which case we simply do not prefill.
        String remembered = RememberMeStore.readUsername();
        if (remembered != null) {
            usernameField.setText(remembered);
        }

        loginBtn = new JButton("Log In");
        loginBtn.setPreferredSize(new Dimension(300, 40));
        loginBtn.setBackground(PRIMARY_BLUE);
        loginBtn.setForeground(WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rememberMe.setForeground(TEXT_DARK);
        rememberMe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // "Remember me" sits on the same row, immediately to the left of the Log In button
        // (centered as a group so it reads as one control bar next to the fields above).
        JPanel loginRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        loginRowPanel.setBackground(WHITE);
        loginRowPanel.add(rememberMe);
        loginRowPanel.add(loginBtn);

        registerLink = new JLabel("<html><u>Don't have an account? Register</u></html>");
        registerLink.setForeground(PRIMARY_BLUE);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(loginTitle);
        card.add(subtitle, "gapbottom 10");
        card.add(usernameField, fieldConstraints);
        card.add(passwordField, fieldConstraints);
        card.add(loginRowPanel, "gaptop 10");
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

            // (The old hardcoded staff/admin UI-test bypass was removed: staff/staff is a real
            //  seeded account, and setLoggedInUser now routes every role to the right screen.)

            try {
                UserDAO userDAO = new UserDAO();
                AuthenticationService authService = new AuthenticationService(userDAO);
                User loggedInUser = authService.login(username, password);

                if (loggedInUser != null) {
                    // Security logs: record the successful login (never affects login behavior).
                    recordLoginEvent(username, SecurityLog.LOGIN_SUCCESS, roleForUser(loggedInUser));
                    // T7: persist (or forget) the remembered username. Only the username is
                    // stored -- never the password and never its hash.
                    RememberMeStore.writeUsername(rememberMe.isSelected() ? loggedInUser.getUsername() : null);
                    // setLoggedInUser stores the user and routes to the role's dashboard.
                    parentFrame.setLoggedInUser(loggedInUser);
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Login Successful! Welcome back, " + loggedInUser.getUsername());
                } else {
                    // Security logs: record the failed login (never affects login behavior).
                    recordLoginEvent(username, SecurityLog.LOGIN_FAIL, "");
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** Inserts a LOGIN_SUCCESS/LOGIN_FAIL security-log row; a failure to log must never block login. */
    private void recordLoginEvent(String username, String action, String role) {
        try {
            SecurityLogDAO dao = new SecurityLogDAO(DatabaseConnection.getSingleton());
            dao.insert(new SecurityLog(action, username, role, SecurityLog.LOGIN_SUCCESS.equals(action)));
        } catch (Exception ignored) {
            // Logging is best-effort.
        }
    }

    /** The role name for a user's role id, or "" when unresolved. */
    private static String roleForUser(User user) {
        try {
            Role role = new RoleDAO().findById(user.getRoleId());
            return role == null ? "" : role.getRoleName();
        } catch (Exception e) {
            return "";
        }
    }

    public void clearFields() {
        passwordField.setText("");
        // Restore the remembered username rather than wiping it, so the T7 prefill survives
        // navigation/logout (MainFrame.showScreen(LOGIN_PAGE) and triggerLogout both call this).
        String remembered = RememberMeStore.readUsername();
        usernameField.setText(remembered != null ? remembered : "");
    }
}
