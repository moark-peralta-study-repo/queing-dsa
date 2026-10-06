package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class PatientQueuePanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JLabel backBtn;
    private JComboBox<String> departmentCombo;
    private JTextArea notesArea;
    private JButton joinQueueBtn;

    public PatientQueuePanel(MainFrame parentFrame) {
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
        
        JLabel titleLabel = new JLabel("HOSPITAL - JOIN LIVE QUEUE");
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

        JPanel card = new JPanel(new MigLayout("wrap 2, insets 35 45 35 45", "[right]15[left, grow, fill]", "[]20[]15[]25[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel formTitle = new JLabel("Get a Queue Number");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        formTitle.setForeground(TEXT_DARK);
        
        JLabel formSubtitle = new JLabel("Join the walk-in line for today.");
        formSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formSubtitle.setForeground(new Color(113, 128, 150));

        departmentCombo = new JComboBox<>(new String[]{"Select Department...", "Emergency Care", "Cardiology", "Pediatrics", "General Surgery", "Radiology", "Pharmacy"});
        departmentCombo.setBackground(WHITE);
        
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        joinQueueBtn = new JButton("Generate Queue Ticket");
        joinQueueBtn.setBackground(PRIMARY_BLUE);
        joinQueueBtn.setForeground(WHITE);
        joinQueueBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        joinQueueBtn.setFocusPainted(false);
        joinQueueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel titlePanel = new JPanel(new MigLayout("insets 0, wrap 1", "[center]", "[]2[]"));
        titlePanel.setOpaque(false);
        titlePanel.add(formTitle);
        titlePanel.add(formSubtitle);

        card.add(titlePanel, "span 2, center, gapbottom 15");
        card.add(new JLabel("Department:")); card.add(departmentCombo, "width 260!, height 38!");
        card.add(new JLabel("Current Symptoms:")); card.add(notesScroll, "width 260!, height 80!");
        card.add(joinQueueBtn, "span 2, center, width 260!, height 42!");

        centerWrapper.add(card);
        
        JScrollPane mainScroll = new JScrollPane(centerWrapper);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("DASHBOARD_PAGE");
            }
        });

        joinQueueBtn.addActionListener(e -> {
            String department = (String) departmentCombo.getSelectedItem();
            String notes = notesArea.getText().trim();

            if (departmentCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Please select a department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String deptInitial = department.substring(0, 1).toUpperCase();
                String generatedQueueNo = deptInitial + "-" + (int)(Math.random() * 100 + 100);

                JOptionPane.showMessageDialog(this, 
                    "You have successfully joined the queue!\n\n" +
                    "Department: " + department + "\n" +
                    "Your Queue Number: " + generatedQueueNo + "\n\n" +
                    "Please wait for your number to be called.", 
                    "Ticket Generated", JOptionPane.INFORMATION_MESSAGE);
                
                departmentCombo.setSelectedIndex(0);
                notesArea.setText("");
                parentFrame.showScreen("DASHBOARD_PAGE");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error joining queue: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- NEW METHOD: Preloads the dropdown menu from the dashboard click ---
    public void preselectDepartment(String departmentName) {
        if (departmentCombo != null) {
            departmentCombo.setSelectedItem(departmentName);
        }
    }
}