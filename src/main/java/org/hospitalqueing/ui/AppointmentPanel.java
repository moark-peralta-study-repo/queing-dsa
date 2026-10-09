package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import org.hospitalqueing.controller.AppointmentController;
import org.hospitalqueing.service.AppointmentService;
import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.User;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentPanel extends JPanel {
    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private JLabel backBtn;
    private JComboBox<String> departmentCombo;
    private JComboBox<String> doctorCombo;
    private raven.datetime.DatePicker dateField;
    private JComboBox<String> timeSlotCombo;
    private JTextArea notesArea;
    private JButton submitAppointmentBtn;
    private MainFrame parentFrame;

    public AppointmentPanel(MainFrame parentFrame) {
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

        JPanel card = new JPanel(new MigLayout("wrap 2, insets 30 40 30 40", "[right]15[left, grow, fill]", "[]15[]15[]15[]15[]15[]25[]"));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel formTitle = new JLabel("Schedule an Appointment");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        formTitle.setForeground(TEXT_DARK);

        departmentCombo = new JComboBox<>(loadDepartments());
        departmentCombo.setBackground(WHITE);

        // Doctor combo is populated dynamically per selected department.
        doctorCombo = new JComboBox<>(new String[]{"Select a Doctor..."});
        doctorCombo.setBackground(WHITE);

        dateField = new raven.datetime.DatePicker();
        dateField.setDateFormat("yyyy-MM-dd");
        dateField.setCloseAfterSelected(true);
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
        card.add(new JLabel("Preferred Doctor:")); card.add(doctorCombo, "width 250!, height 35!");
        card.add(new JLabel("Date:")); card.add(dateField, "width 250!, height 35!");
        card.add(new JLabel("Time Slot:")); card.add(timeSlotCombo, "width 250!, height 35!");
        card.add(new JLabel("Notes / Symptoms:")); card.add(notesScroll, "width 250!, height 70!");
        card.add(submitAppointmentBtn, "span 2, center, width 250!, height 40!");

        centerWrapper.add(card);

        JScrollPane mainScroll = new JScrollPane(centerWrapper);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            mainScroll.getVerticalScrollBar().setValue(0);
            mainScroll.getViewport().setViewPosition(new Point(0, 0));
            refreshDoctors();
        });

        // --- 3. WIRING ACTIONS ---
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showScreen("DASHBOARD_PAGE");
            }
        });

        departmentCombo.addActionListener(e -> refreshDoctors());

        submitAppointmentBtn.addActionListener(e -> submitAppointment());
    }

    private String[] loadDepartments() {
        String[] names = UiData.departmentNames();
        if (names.length == 0) {
            // No departments seeded; fall back to a safe default set so the form is usable.
            return new String[]{"Cardiology", "General Medicine", "Pediatrics", "Orthopedics"};
        }
        return names;
    }

    /** Re-populates the doctor combo for the currently selected department. */
    private void refreshDoctors() {
        if (departmentCombo == null || doctorCombo == null) return;
        String deptName = (String) departmentCombo.getSelectedItem();
        String[] labels = UiData.doctorLabelsForDepartment(deptName);
        doctorCombo.removeAllItems();
        doctorCombo.addItem("Select a Doctor...");
        for (String l : labels) doctorCombo.addItem(l);
        doctorCombo.setSelectedIndex(0);
    }

    private void submitAppointment() {
        String department = (String) departmentCombo.getSelectedItem();
        int doctorIndex = doctorCombo.getSelectedIndex();
        int[] doctorIds = UiData.doctorIdsForDepartment(department);
        int selectedDoctorId = doctorIndex > 0 && doctorIndex - 1 < doctorIds.length
                ? doctorIds[doctorIndex - 1] : -1;
        LocalDate selectedDate = dateField.getSelectedDate();
        String timeSlot = (String) timeSlotCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        // GUARDRAIL 1: Empty Field Check
        if (department == null || selectedDate == null || notes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a department, pick a date, and fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // GUARDRAIL 2: Past-Date Prevention (the picker guarantees a valid date)
        LocalDate appointmentDate = selectedDate;
        if (appointmentDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Appointments cannot be booked in the past.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Translate the UI dropdown String into a LocalTime object for the Database.
        LocalTime appointmentTime = LocalTime.of(9, 0);
        if (timeSlot != null) {
            if (timeSlot.contains("09:00 AM")) appointmentTime = LocalTime.of(9, 0);
            else if (timeSlot.contains("10:00 AM")) appointmentTime = LocalTime.of(10, 0);
            else if (timeSlot.contains("01:00 PM")) appointmentTime = LocalTime.of(13, 0);
            else if (timeSlot.contains("02:00 PM")) appointmentTime = LocalTime.of(14, 0);
        }

        try {
            // The appointments table requires a real patient_id (FK to patients), a real
            // service_id (FK to services), and doctor_id is optional.
            User loggedIn = parentFrame.getLoggedInUser();
            int patientId = UiData.patientIdForUser(loggedIn);
            if (patientId < 0) {
                throw new Exception("No patient profile found for the logged-in user. Please re-register or contact support.");
            }
            int serviceId = UiData.serviceIdForDepartment(department);
            if (serviceId < 0) {
                throw new Exception("Department \"" + department + "\" has no active service in the system. Please pick another department.");
            }

            AppointmentDAO appointmentDAO = new AppointmentDAO();
            AppointmentService appointmentService = new AppointmentService(appointmentDAO);
            AppointmentController appointmentController = new AppointmentController(appointmentService);

            Appointment appointment = new Appointment();
            appointment.setPatientId(patientId);
            appointment.setServiceId(serviceId);
            if (selectedDoctorId > 0) {
                appointment.setDoctorId(selectedDoctorId);
            }
            appointment.setAppointmentDate(appointmentDate);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus("SCHEDULED");

            appointmentController.createAppointment(appointment);

            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!\nService: " + department + "\nDate: " + appointmentDate + " at " + appointmentTime);

            doctorCombo.setSelectedIndex(0);
            dateField.clearSelectedDate();
            notesArea.setText("");
            departmentCombo.setSelectedIndex(0);
            timeSlotCombo.setSelectedIndex(0);

            parentFrame.showScreen("DASHBOARD_PAGE");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error booking appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JLabel getBackBtn() { return backBtn; }
}