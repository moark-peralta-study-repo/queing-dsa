package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JLabel backBtn;
    
    // Form fields
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField birthDateField;
    private JComboBox<String> sexComboBox;
    private JPasswordField newPasswordField;
    private JButton saveBtn;

    private MainFrame parentFrame;

    public ProfilePanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
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
        
        JLabel titleLabel = new JLabel("HOSPITAL - MY PROFILE");
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        headerPanel.add(backBtn);
        headerPanel.add(logoLabel);
        headerPanel.add(titleLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(new JSeparator(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. FORM BODY CARD ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(240, 244, 248));

        JPanel card = new JPanel(new MigLayout("wrap 2, insets 30 40 30 40", "[right, 120]15[left, grow, fill]", "[]20[]15[]15[]15[]15[]15[]15[]20[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel formTitle = new JLabel("Personal Information");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        formTitle.setForeground(TEXT_DARK);

        usernameField = new JTextField();
        usernameField.setEditable(false); // Username usually cannot be changed
        usernameField.setBackground(new Color(245, 245, 245));
        
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        phoneField = new JTextField();
        birthDateField = new JTextField();
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        
        newPasswordField = new JPasswordField();
        newPasswordField.setToolTipText("Leave blank to keep current password");

        String fieldConstraints = "width 250!, height 35!";

        card.add(formTitle, "span 2, center, gapbottom 10");
        
        card.add(new JLabel("Username:")); card.add(usernameField, fieldConstraints);
        card.add(new JLabel("First Name:")); card.add(firstNameField, fieldConstraints);
        card.add(new JLabel("Last Name:")); card.add(lastNameField, fieldConstraints);
        card.add(new JLabel("Phone Number:")); card.add(phoneField, fieldConstraints);
        card.add(new JLabel("Birth Date:")); card.add(birthDateField, fieldConstraints);
        card.add(new JLabel("Sex:")); card.add(sexComboBox, fieldConstraints);
        card.add(new JLabel("New Password:")); card.add(newPasswordField, fieldConstraints);

        saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(PRIMARY_BLUE);
        saveBtn.setForeground(WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        card.add(saveBtn, "span 2, center, width 250!, height 40!");

        centerWrapper.add(card);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Lock scrollbar to top
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
        });

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("DASHBOARD_PAGE");
            }
        });

        saveBtn.addActionListener(e -> {
            // TODO for Backend Phase: Hook this up to UserDAO and PatientDAO to update the database
            JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
            parentFrame.showScreen("DASHBOARD_PAGE");
        });
    }

    // This method is called by MainFrame every time this screen opens to load the fresh data
    public void loadUserData() {
        if (parentFrame.getLoggedInUser() != null) {
            usernameField.setText(parentFrame.getLoggedInUser().getUsername());
            
            // TODO for Backend Phase: Fetch the Patient object using getLoggedInUser().getUserId()
            // patient = patientDAO.getPatientByUserId(parentFrame.getLoggedInUser().getUserId());
            // firstNameField.setText(patient.getFirstName());
            // lastNameField.setText(patient.getLastName());
            // etc...
            
            newPasswordField.setText(""); // Always clear password field on load
        }
    }
}