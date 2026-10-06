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
    
    // Form fields
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JTextField usernameField;
    private JTextField phoneField;
    private JTextField birthDateField;
    private JComboBox<String> sexComboBox;
    private JPasswordField passwordField;
    
    private JCheckBox termsCheckbox;
    private JButton submitBtn;

    public RegisterPanel() {
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // --- 1. HEADER (Consistent with Login and Appointment screens) ---
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

        // --- 2. FORM BODY CARD (Centered modern card layout) ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(240, 244, 248));

        JPanel card = new JPanel(new MigLayout("wrap 2, insets 25 35 25 35, center", "[right, 110]15[left, grow, fill]", "[]12[]10[]10[]10[]10[]10[]10[]15[]15[]10[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel formTitle = new JLabel("Create Account");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        formTitle.setForeground(TEXT_DARK);

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();
        usernameField = new JTextField();
        phoneField = new JTextField();
        birthDateField = new JTextField();
        birthDateField.setToolTipText("YYYY-MM-DD");
        sexComboBox = new JComboBox<>(new String[]{"Select", "Male", "Female", "Other"});
        passwordField = new JPasswordField();

        String fieldConstraints = "width 240!, height 32!";

        card.add(formTitle, "span 2, center, gapbottom 5");
        
        card.add(new JLabel("First Name:")); card.add(firstNameField, fieldConstraints);
        card.add(new JLabel("Middle Name:")); card.add(middleNameField, fieldConstraints);
        card.add(new JLabel("Last Name:")); card.add(lastNameField, fieldConstraints);
        card.add(new JLabel("Username:")); card.add(usernameField, fieldConstraints);
        card.add(new JLabel("Phone Number:")); card.add(phoneField, fieldConstraints);
        card.add(new JLabel("Birth Date (YYYY-MM-DD):")); card.add(birthDateField, fieldConstraints);
        card.add(new JLabel("Sex:")); card.add(sexComboBox, fieldConstraints);
        card.add(new JLabel("Password:")); card.add(passwordField, fieldConstraints);

        termsCheckbox = new JCheckBox("I agree to the terms and conditions");
        termsCheckbox.setBackground(WHITE);
        card.add(termsCheckbox, "span 2, center, gaptop 5");

        submitBtn = new JButton("Create Account");
        submitBtn.setBackground(PRIMARY_BLUE);
        submitBtn.setForeground(WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(submitBtn, "span 2, center, width 260!, height 38!");

        loginLink = new JLabel("<html><u>Already have an account? Log in</u></html>");
        loginLink.setForeground(PRIMARY_BLUE);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(loginLink, "span 2, center");

        centerWrapper.add(card);

        // Scroll Pane Wrapper to handle fit smoothly
        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);

        // Force scrollbar to stay locked firmly at the absolute top on load
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
        });
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