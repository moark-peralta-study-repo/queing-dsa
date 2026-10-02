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
    private JPanel topContainer; 
    private org.hospitalqueing.model.User loggedInUser;

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

        add(topContainer, BorderLayout.NORTH);

        // --- 2. APP STATE SCREENS (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        
        homePage = new HomePagePanel();
        loginCard = new LoginPanel(this);
        registerCard = new RegisterPanel();
        dashboardCard = new PatientDashboardPanel(this);
        appointmentCard = new AppointmentPanel(this);

        mainContentPanel.add(homePage, "LANDING_PAGE");
        mainContentPanel.add(loginCard, "LOGIN_PAGE");
        mainContentPanel.add(registerCard, "REGISTER_PAGE");
        mainContentPanel.add(dashboardCard, "DASHBOARD_PAGE");
        mainContentPanel.add(appointmentCard, "APPOINTMENT_PAGE");

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
                newUser.setRoleId(3); 
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
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainContentPanel, screenName);
        if ("LANDING_PAGE".equals(screenName)) {
            topContainer.setVisible(true);
        } else {
            topContainer.setVisible(false);
        }
        
        // Automatically clear login text boxes whenever the login screen is displayed
        if ("LOGIN_PAGE".equals(screenName) && loginCard != null) {
            loginCard.clearFields();
        }
    }

    public void setLoggedInUser(org.hospitalqueing.model.User user) {
        this.loggedInUser = user;
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