package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import org.hospitalqueing.controller.AppointmentController;
import org.hospitalqueing.service.AppointmentService;
import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.model.Appointment;

import javax.swing.*;
import java.awt.*;

public class AppointmentPanel extends JPanel {
    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JLabel backBtn;
    private JComboBox<String> departmentCombo;
    private JTextField doctorField;
    private JTextField dateField;
    private JComboBox<String> timeSlotCombo;
    private JTextArea notesArea;
    private JButton submitAppointmentBtn;

    public AppointmentPanel(MainFrame parentFrame) {
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
        
        JLabel titleLabel = new JLabel("HOSPITAL - APPOINTMENT BOOKING");
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

        JPanel card = new JPanel(new MigLayout("wrap 2, insets 30 40 30 40", "[right]15[left, grow, fill]", "[]15[]15[]15[]15[]25[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel formTitle = new JLabel("Schedule an Appointment");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        formTitle.setForeground(TEXT_DARK);

        departmentCombo = new JComboBox<>(new String[]{"Emergency Care", "Cardiology", "Pediatrics", "General Surgery", "Radiology", "Pharmacy"});
        doctorField = new JTextField();
        dateField = new JTextField();
        dateField.setToolTipText("YYYY-MM-DD");
        timeSlotCombo = new JComboBox<>(new String[]{"09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "01:00 PM - 02:00 PM", "02:00 PM - 03:00 PM"});
        
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        submitAppointmentBtn = new JButton("Confirm Appointment");
        submitAppointmentBtn.setBackground(PRIMARY_BLUE);
        submitAppointmentBtn.setForeground(WHITE);
        submitAppointmentBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitAppointmentBtn.setFocusPainted(false);
        submitAppointmentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(formTitle, "span 2, center, gapbottom 10");
        card.add(new JLabel("Department:")); card.add(departmentCombo, "width 250!, height 35!");
        card.add(new JLabel("Preferred Doctor:")); card.add(doctorField, "width 250!, height 35!");
        card.add(new JLabel("Date (YYYY-MM-DD):")); card.add(dateField, "width 250!, height 35!");
        card.add(new JLabel("Time Slot:")); card.add(timeSlotCombo, "width 250!, height 35!");
        card.add(new JLabel("Notes / Symptoms:")); card.add(notesScroll, "width 250!, height 70!");
        card.add(submitAppointmentBtn, "span 2, center, width 250!, height 40!");

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

        submitAppointmentBtn.addActionListener(e -> {
            String date = dateField.getText().trim();
            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an appointment date.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                AppointmentService appointmentService = new AppointmentService(appointmentDAO);
                AppointmentController appointmentController = new AppointmentController(appointmentService);

                Appointment appointment = new Appointment();
                appointment.setPatientId(1); 
                appointment.setStatus("Scheduled");
                
                appointmentController.createAppointment(appointment);

                JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!");
                parentFrame.showScreen("DASHBOARD_PAGE");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error booking appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public JLabel getBackBtn() { return backBtn; }
}