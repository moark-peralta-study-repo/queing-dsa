package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    // Navigation triggers
    private JLabel backBtn;
    private JLabel loginLink;
    
    // Form fields mapped to DatabaseConnection schema
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JTextField usernameField; // Dedicated Username field
    private JTextField phoneField;
    private JTextField birthDateField;
    private JComboBox<String> sexComboBox;
    private JPasswordField passwordField;
    
    private JCheckBox termsCheckbox;
    private JButton submitBtn;

    public RegisterPanel() {
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- 1. HEADER (Matches wireframe: Back Arrow + Logo) ---
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

        // Separator
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(new JSeparator(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. FORM BODY (Centered, matching wireframe layout) ---
        JPanel formContainer = new JPanel(new MigLayout("wrap, insets 40 20 40 20", "[center]", "[]20[]"));
        formContainer.setBackground(WHITE);

        JLabel createAccTitle = new JLabel("CREATE ACCOUNT");
        createAccTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        createAccTitle.setForeground(TEXT_DARK);
        formContainer.add(createAccTitle, "wrap, gapbottom 20");

        // Inner form panel holding the actual inputs
        JPanel form = new JPanel(new MigLayout("wrap 2, insets 0", "[right]15[left, grow, fill]", "[]15[]"));
        form.setBackground(WHITE);
        
        String fieldConstraints = "width 250:300:300, height 35!";

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();
        usernameField = new JTextField(); // Standalone Username
        phoneField = new JTextField();
        birthDateField = new JTextField();
        birthDateField.setToolTipText("YYYY-MM-DD");
        sexComboBox = new JComboBox<>(new String[]{"Select", "Male", "Female", "Other"});
        passwordField = new JPasswordField();

        // Add fields to form
        form.add(new JLabel("First Name:")); form.add(firstNameField, fieldConstraints);
        form.add(new JLabel("Middle Name:")); form.add(middleNameField, fieldConstraints);
        form.add(new JLabel("Last Name:")); form.add(lastNameField, fieldConstraints);
        form.add(new JLabel("Username:")); form.add(usernameField, fieldConstraints); // Clear separate username
        form.add(new JLabel("Phone Number:")); form.add(phoneField, fieldConstraints);
        form.add(new JLabel("Birth Date (YYYY-MM-DD):")); form.add(birthDateField, fieldConstraints);
        form.add(new JLabel("Sex:")); form.add(sexComboBox, fieldConstraints);
        form.add(new JLabel("Password:")); form.add(passwordField, fieldConstraints);

        formContainer.add(form);

        // Checkbox, Submit Button, and Login Link
        JPanel bottomActionPanel = new JPanel(new MigLayout("wrap, center", "[center]", "[]15[]10[]"));
        bottomActionPanel.setBackground(WHITE);

        termsCheckbox = new JCheckBox("I agree to the terms and conditions");
        termsCheckbox.setBackground(WHITE);
        bottomActionPanel.add(termsCheckbox);

        submitBtn = new JButton("Create Account");
        submitBtn.setPreferredSize(new Dimension(300, 40));
        submitBtn.setBackground(PRIMARY_BLUE);
        submitBtn.setForeground(WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.setFocusPainted(false);
        bottomActionPanel.add(submitBtn);

        loginLink = new JLabel("<html><u>Already have an account? Log in</u></html>");
        loginLink.setForeground(PRIMARY_BLUE);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bottomActionPanel.add(loginLink);

        formContainer.add(bottomActionPanel);

        // Scroll Pane Setup (Scrollable logic retained, but vertical scrollbar hidden visually)
        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 
        add(scrollPane, BorderLayout.CENTER);
    }

    // --- Getters for MainFrame routing & database mapping ---
    public JLabel getBackBtn() { return backBtn; }
    public JLabel getLoginLink() { return loginLink; }
    public JButton getSubmitBtn() { return submitBtn; }

    public String getFirstName() { return firstNameField.getText(); }
    public String getMiddleName() { return middleNameField.getText(); }
    public String getLastName() { return lastNameField.getText(); }
    public String getUsername() { return usernameField.getText(); }
    public String getPhone() { return phoneField.getText(); }
    public String getBirthDate() { return birthDateField.getText(); }
    public String getSex() { return (String) sexComboBox.getSelectedItem(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public boolean isTermsAccepted() { return termsCheckbox.isSelected(); }
}