package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class StaffWalkInPanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);

    private JTextField fullNameField;
    private JTextField phoneField;
    private JComboBox<String> departmentCombo;
    private JButton registerBtn;

    public StaffWalkInPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_LIGHT);

        // Center Wrapper to hold the form card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BACKGROUND_LIGHT);

        // The Form Card
        JPanel card = new JPanel(new MigLayout("wrap 1, insets 40 50 40 50, center", "[center, fill]", "[]10[]20[]10[]10[]10[]25[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));

        // Form Icon & Title
        JLabel iconLbl = new JLabel("📝");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        
        JLabel title = new JLabel("Walk-in Registration");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        
        JLabel subtitle = new JLabel("Add a patient without an appointment directly to the queue.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(113, 128, 150));

        // Form Fields
        fullNameField = new JTextField();
        fullNameField.putClientProperty("JTextField.placeholderText", "Enter Full Name");
        
        phoneField = new JTextField();
        phoneField.putClientProperty("JTextField.placeholderText", "Enter Phone Number");
        
        String[] departments = {"Select Department...", "Emergency Care", "Cardiology", "Pediatrics", "General Surgery", "Radiology", "Pharmacy"};
        departmentCombo = new JComboBox<>(departments);
        departmentCombo.setBackground(WHITE);

        String fieldConstraints = "width 300!, height 40!";

        // Register Button
        registerBtn = new JButton("Register to Queue");
        registerBtn.setBackground(PRIMARY_BLUE);
        registerBtn.setForeground(WHITE);
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add components to card
        card.add(iconLbl);
        card.add(title);
        card.add(subtitle, "gapbottom 15");
        
        card.add(new JLabel("Full Name"), "left, gapbottom 2");
        card.add(fullNameField, fieldConstraints);
        
        card.add(new JLabel("Phone Number"), "left, gaptop 10, gapbottom 2");
        card.add(phoneField, fieldConstraints);
        
        card.add(new JLabel("Department"), "left, gaptop 10, gapbottom 2");
        card.add(departmentCombo, fieldConstraints);
        
        card.add(registerBtn, "gaptop 20, height 45!");

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // --- WIRING (UI ONLY & MVC PREP) ---
        registerBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String department = (String) departmentCombo.getSelectedItem();

            // GUARDRAIL 1: Empty Field Validation
            if (fullName.isEmpty() || departmentCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in the patient's name and select a department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // --- TEMPORARY BYPASS FOR TEAMMATE ---
                // Your teammate can uncomment this block and adjust the variable names
                // when they create the Queue/Walk-in backend logic.
                /*
                QueueDAO queueDAO = new QueueDAO();
                QueueService queueService = new QueueService(queueDAO);
                QueueController queueController = new QueueController(queueService);

                QueueEntry walkIn = new QueueEntry();
                walkIn.setPatientName(fullName);
                walkIn.setPhoneNumber(phone);
                walkIn.setDepartment(department);
                walkIn.setDate(java.time.LocalDate.now());
                walkIn.setTime(java.time.LocalTime.now());
                walkIn.setStatus("WAITING");
                walkIn.setType("WALK_IN");

                // Assuming the controller returns the generated Queue Number:
                // String generatedQueueNo = queueController.registerWalkIn(walkIn);
                */

                // Simulated Queue Number for UI testing (e.g., "C-142" for Cardiology)
                String deptInitial = department.substring(0, 1).toUpperCase();
                String generatedQueueNo = deptInitial + "-" + (int)(Math.random() * 100 + 100);

                // Display success message with the Queue Number
                JOptionPane.showMessageDialog(this, 
                    "Walk-in successfully registered!\n\n" +
                    "Patient: " + fullName + "\n" +
                    "Department: " + department + "\n" +
                    "Queue Number: " + generatedQueueNo, 
                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear form after success
                fullNameField.setText("");
                phoneField.setText("");
                departmentCombo.setSelectedIndex(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error registering walk-in: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}