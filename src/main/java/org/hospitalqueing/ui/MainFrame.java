package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);   
    private final Color TEXT_DARK = new Color(45, 55, 72);
    
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private HomePagePanel homePage;
    private LoginPanel loginCard;
    private RegisterPanel registerCard;
    private PatientDashboardPanel dashboardCard;
    private AppointmentPanel appointmentCard;
    private PatientQueuePanel patientQueueCard; // Elevated to class level
    private AdminDashboardPanel adminDashboardCard;
    private DoctorDashboardPanel doctorDashboardCard;
    private WaitingRoomPanel waitingRoomCard;
    private JPanel topContainer; 
    private org.hospitalqueing.model.User loggedInUser;
    private org.hospitalqueing.web.WebServer webServer;
    private JLabel serverStatusLabel;
    private JButton serverToggle;

    public MainFrame() {
        setTitle("Hospital Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- 1. PERSISTENT GLOBAL NAVBAR (Landing Page Only) ---
        JPanel navPanel = new JPanel(new MigLayout("insets 15 30 15 30, aligny center", "[left]push[right]", "[center]"));
        navPanel.setBackground(Color.WHITE);

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

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        linksPanel.setOpaque(false);
        
        JLabel homeLink = createNavLink("Home");
        JLabel servicesLink = createNavLink("Services");
        JLabel doctorsLink = createNavLink("Doctors");
        JLabel aboutLink = createNavLink("About");
        
        linksPanel.add(homeLink);
        linksPanel.add(servicesLink);
        linksPanel.add(doctorsLink);
        linksPanel.add(aboutLink);
        
        navPanel.add(brandPanel);
        navPanel.add(linksPanel);

        topContainer = new JPanel(new BorderLayout());
        topContainer.add(navPanel, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(230, 230, 230));
        topContainer.add(separator, BorderLayout.SOUTH);

        // --- Server control bar (ALWAYS visible, above the nav) ---
        serverStatusLabel = new JLabel("Starting…");
        serverStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        serverStatusLabel.setForeground(TEXT_DARK);
        serverToggle = new JButton("Start");
        serverToggle.setFont(new Font("SansSerif", Font.BOLD, 11));
        serverToggle.setFocusPainted(false);
        serverToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        serverToggle.addActionListener(e -> toggleServer());
        JPanel serverBar = new JPanel(new BorderLayout(8, 0));
        serverBar.setBackground(new Color(245, 245, 245));
        serverBar.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
        serverBar.add(serverStatusLabel, BorderLayout.CENTER);
        serverBar.add(serverToggle, BorderLayout.EAST);

        add(serverBar, BorderLayout.NORTH);
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(serverBar, BorderLayout.NORTH);
        northWrap.add(topContainer, BorderLayout.CENTER);
        add(northWrap, BorderLayout.NORTH);

        // --- 2. APP STATE SCREENS (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        
        homePage = new HomePagePanel();
        loginCard = new LoginPanel(this);
        registerCard = new RegisterPanel();
        dashboardCard = new PatientDashboardPanel(this);
        appointmentCard = new AppointmentPanel(this);
        
        ProfilePanel profileCard = new ProfilePanel(this); 
        AppointmentHistoryPanel historyCard = new AppointmentHistoryPanel(this);
        StaffDashboardPanel staffDashboardCard = new StaffDashboardPanel(this);
        adminDashboardCard = new AdminDashboardPanel(this);
        doctorDashboardCard = new DoctorDashboardPanel(this);
        
        patientQueueCard = new PatientQueuePanel(this); // Now saves to class variable
        waitingRoomCard = new WaitingRoomPanel();
        TrashBinPanel trashCard = new TrashBinPanel(this);

        mainContentPanel.add(homePage, "LANDING_PAGE");
        mainContentPanel.add(loginCard, "LOGIN_PAGE");
        mainContentPanel.add(registerCard, "REGISTER_PAGE");
        mainContentPanel.add(dashboardCard, "DASHBOARD_PAGE");
        mainContentPanel.add(appointmentCard, "APPOINTMENT_PAGE");
        mainContentPanel.add(profileCard, "PROFILE_PAGE");
        mainContentPanel.add(historyCard, "HISTORY_PAGE");
        mainContentPanel.add(staffDashboardCard, "STAFF_DASHBOARD");
        mainContentPanel.add(adminDashboardCard, "ADMIN_DASHBOARD");
        mainContentPanel.add(doctorDashboardCard, "DOCTOR_DASHBOARD");
        mainContentPanel.add(patientQueueCard, "QUEUE_STATUS_PAGE");
        mainContentPanel.add(waitingRoomCard, "WAITING_ROOM");
        mainContentPanel.add(trashCard, "TRASH_BIN");

        add(mainContentPanel, BorderLayout.CENTER);

        // --- 3. WIRING SCROLLING ---
        homeLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showHomeAndScroll(homePage::scrollToTop); }
        });
        
        servicesLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showHomeAndScroll(homePage::scrollToServices); }
        });

        doctorsLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showHomeAndScroll(homePage::scrollToDoctors); }
        });

        aboutLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showHomeAndScroll(homePage::scrollToAbout); }
        });

        // --- 4. ROUTING & REGISTRATION LOGIC ---
        homePage.getBottomLoginBtn().addActionListener(e -> showScreen("LOGIN_PAGE"));
        homePage.getCreateAccBtn().addActionListener(e -> showScreen("REGISTER_PAGE"));

        registerCard.getBackBtn().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showScreen("LANDING_PAGE"); }
        });

        registerCard.getLoginLink().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showScreen("LOGIN_PAGE"); }
        });

        registerCard.getSubmitBtn().addActionListener(e -> {
            if (!registerCard.isTermsAccepted()) {
                JOptionPane.showMessageDialog(this, "Please accept the terms and conditions.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (registerCard.getUsername().trim().isEmpty() || registerCard.getPassword().trim().isEmpty() ||
                registerCard.getFirstName().trim().isEmpty() || registerCard.getLastName().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                org.hospitalqueing.dao.UserDAO userDAO = new org.hospitalqueing.dao.UserDAO();
                org.hospitalqueing.dao.PatientDAO patientDAO = new org.hospitalqueing.dao.PatientDAO();

                org.hospitalqueing.service.UserService userService = new org.hospitalqueing.service.UserService(userDAO);
                org.hospitalqueing.service.PatientService patientService = new org.hospitalqueing.service.PatientService(patientDAO);
                org.hospitalqueing.service.AuthenticationService authService = new org.hospitalqueing.service.AuthenticationService(userDAO);

                org.hospitalqueing.controller.UserController userController = new org.hospitalqueing.controller.UserController(userService);
                org.hospitalqueing.controller.PatientController patientController = new org.hospitalqueing.controller.PatientController(patientService);

                String username = registerCard.getUsername().trim();

                for (org.hospitalqueing.model.User u : userController.getAllUsers()) {
                    if (u.getUsername().equalsIgnoreCase(username)) {
                        JOptionPane.showMessageDialog(this, "This username is already registered. Please choose another or log in.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                org.hospitalqueing.model.User newUser = new org.hospitalqueing.model.User();
                newUser.setUsername(username);
                newUser.setPasswordHash(authService.hashPassword(registerCard.getPassword()));
                // New accounts are patients. Use the seeded role id (PATIENT) when present.
                int patientRoleId = org.hospitalqueing.ui.UiData.roleIdByName("PATIENT");
                newUser.setRoleId(patientRoleId > 0 ? patientRoleId : 3);
                newUser.setActive(true);
                userController.createUser(newUser);

                int generatedUserId = -1;
                for (org.hospitalqueing.model.User u : userController.getAllUsers()) {
                    if (u.getUsername().equalsIgnoreCase(username)) {
                        generatedUserId = u.getUserId();
                        break;
                    }
                }

                if (generatedUserId != -1) {
                    org.hospitalqueing.model.Patient patient = new org.hospitalqueing.model.Patient();
                    patient.setUserId(generatedUserId);
                    patient.setFirstName(registerCard.getFirstName().trim());
                    patient.setMiddleName(registerCard.getMiddleName() != null ? registerCard.getMiddleName().trim() : "");
                    patient.setLastName(registerCard.getLastName().trim());
                    patient.setBirthDate(registerCard.getBirthDate().trim());
                    patient.setSex(registerCard.getSex());
                    patient.setPhone(registerCard.getPhone().trim());
                    
                    patientController.registerPatient(patient);
                } else {
                    throw new RuntimeException("Could not resolve generated user ID for patient linking.");
                }
                
                JOptionPane.showMessageDialog(this, "Account Created Successfully! Please log in.");
                showScreen("LOGIN_PAGE");
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Registration Failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Auto-start the API server on launch (the phone / a second monitor poll it); show the
        // LAN IP in the status bar so the staff can put it in the phone app's host field.
        toggleServer();

        // Stop the web server cleanly when the window closes.
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (webServer != null) webServer.stop();
            }
        });
    }

    /** Starts/stops the Javalin API server and updates the status bar with the LAN IP to use. */
    private void toggleServer() {
        if (webServer == null) {
            int port = 8080;
            try {
                webServer = new org.hospitalqueing.web.WebServer(port);
                String ip = serverIp();
                serverStatusLabel.setText("API server running — http://" + ip + ":" + port);
                serverStatusLabel.setForeground(new Color(46, 125, 50));
                serverToggle.setText("Stop");
            } catch (Exception ex) {
                webServer = null;
                serverStatusLabel.setText("Server not started (" + rootCause(ex) + ")");
                serverStatusLabel.setForeground(TEXT_DARK);
                serverToggle.setText("Start");
            }
        } else {
            webServer.stop();
            webServer = null;
            serverStatusLabel.setText("API server stopped");
            serverStatusLabel.setForeground(TEXT_DARK);
            serverToggle.setText("Start");
        }
    }

    /** Best-effort LAN IP (0.0.0.0 → loopback). For the phone app's host field. */
    private static String serverIp() {
        try {
            java.net.NetworkInterface ni = java.net.NetworkInterface.getByInetAddress(
                java.net.InetAddress.getLocalHost());
            if (ni != null) {
                for (java.net.InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "localhost";
    }

    private static String rootCause(Throwable t) {
        Throwable x = t;
        while (x.getCause() != null) x = x.getCause();
        return x.getMessage() == null ? x.getClass().getSimpleName() : x.getMessage();
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainContentPanel, screenName);
        if ("LANDING_PAGE".equals(screenName)) {
            topContainer.setVisible(true);
        } else {
            topContainer.setVisible(false);
        }
        
        if ("LOGIN_PAGE".equals(screenName) && loginCard != null) {
            loginCard.clearFields();
        }
        
        if ("PROFILE_PAGE".equals(screenName)) {
            for (Component comp : mainContentPanel.getComponents()) {
                if (comp instanceof ProfilePanel) {
                    ((ProfilePanel) comp).loadUserData();
                }
            }
        }
        
        if ("HISTORY_PAGE".equals(screenName)) {
            for (Component comp : mainContentPanel.getComponents()) {
                if (comp instanceof AppointmentHistoryPanel) {
                    ((AppointmentHistoryPanel) comp).loadHistoryData();
                }
            }
        }
        
        if ("WAITING_ROOM".equals(screenName) && waitingRoomCard != null) {
            waitingRoomCard.refresh();
        }

        if ("TRASH_BIN".equals(screenName)) {
            for (Component comp : mainContentPanel.getComponents()) {
                if (comp instanceof TrashBinPanel) {
                    ((TrashBinPanel) comp).loadTrashData();
                }
            }
        }
    }

    // --- NEW METHOD: Preselects the department and opens the queue screen ---
    public void routeToQueueWithDepartment(String department) {
        if (patientQueueCard != null) {
            patientQueueCard.preselectDepartment(department);
        }
        showScreen("QUEUE_STATUS_PAGE");
    }

    public void setLoggedInUser(org.hospitalqueing.model.User user) {
        this.loggedInUser = user;
        // Land on the right dashboard for the role: admins get the management dashboard,
        // doctors get the per-doctor dashboard, other staff get the daily-operations dashboard.
        if (user != null) {
            if (UiData.isAdmin(user)) {
                showScreen("ADMIN_DASHBOARD");
            } else if (UiData.isDoctor(user)) {
                if (doctorDashboardCard != null) {
                    doctorDashboardCard.reload();
                }
                showScreen("DOCTOR_DASHBOARD");
            } else if (UiData.isStaff(user)) {
                showScreen("STAFF_DASHBOARD");
            } else {
                showScreen("DASHBOARD_PAGE");
            }
        }
    }

    public org.hospitalqueing.model.User getLoggedInUser() {
        return this.loggedInUser;
    }

    public void triggerLogout() {
        loggedInUser = null;
        loginCard.clearFields();
        showScreen("LANDING_PAGE");
    }

    private void showHomeAndScroll(Runnable scrollAction) {
        showScreen("LANDING_PAGE");
        scrollAction.run();
    }

    private JLabel createNavLink(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_DARK);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        label.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { label.setForeground(PRIMARY_BLUE); }
            public void mouseExited(MouseEvent e) { label.setForeground(TEXT_DARK); }
        });
        return label;
    }
}