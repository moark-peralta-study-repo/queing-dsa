package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Counter;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.QueueStatus;
import org.hospitalqueing.service.QueueManagementService;

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

        departmentCombo = new JComboBox<>(buildDepartmentOptions());
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

        // --- WIRING ---
        registerBtn.addActionListener(e -> registerWalkIn());
    }

    private String[] buildDepartmentOptions() {
        String[] names = UiData.departmentNames();
        if (names.length == 0) {
            return new String[]{"Select Department...", "Cardiology", "General Medicine", "Pediatrics", "Orthopedics"};
        }
        String[] withPlaceholder = new String[names.length + 1];
        withPlaceholder[0] = "Select Department...";
        System.arraycopy(names, 0, withPlaceholder, 1, names.length);
        return withPlaceholder;
    }

    private void registerWalkIn() {
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String department = (String) departmentCombo.getSelectedItem();

        // GUARDRAIL 1: Empty Field Validation
        if (fullName.isEmpty() || department == null || department.equals("Select Department...")) {
            JOptionPane.showMessageDialog(this, "Please fill in the patient's name and select a department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int departmentId = UiData.departmentIdByName(department);
            if (departmentId < 0) {
                throw new Exception("Department \"" + department + "\" not found in the system. Please pick another.");
            }
            int serviceId = UiData.serviceIdForDepartmentId(departmentId);
            if (serviceId < 0) {
                throw new Exception("Department \"" + department + "\" has no active service. Please seed one first.");
            }

            // Create a real user + patient row so the queue entry's patient_id FK holds.
            int patientId = UiData.createWalkInPatient(fullName, phone);

            // First active counter for the department (optional; null when none seeded).
            CounterDAO counterDAO = new CounterDAO();
            Integer counterId = null;
            List<Counter> counters = counterDAO.findByDepartment(departmentId);
            if (counters != null) {
                for (Counter c : counters) {
                    if (c.isActive()) {
                        counterId = c.getCounterId();
                        break;
                    }
                }
            }

            QueueEntryDAO queueEntryDAO = new QueueEntryDAO();
            QueueManagementService qms = new QueueManagementService(
                    queueEntryDAO, new QueueEventDAO(), new ServiceDAO(), new DepartmentDAO(), counterDAO);

            QueueEntry entry = new QueueEntry();
            entry.setPatientId(patientId);
            entry.setDepartmentId(departmentId);
            entry.setServiceId(serviceId);
            entry.setCounterId(counterId);
            entry.setQueueDate(LocalDate.now().toString());
            entry.setPriorityType("REGULAR");
            entry.setStatus(QueueStatus.WAITING);
            entry.setDoctorId(null);
            entry.setAppointmentId(null);

            QueueEntry saved = qms.joinQueue(entry);

            // Show the real ticket: scannable QR of the token, not a plain string.
            TicketDialog.show(this,
                    "Walk-in registered successfully!",
                    department,
                    saved.getQueueNumber(),
                    saved.getQrToken(),
                    "Patient: " + fullName);

            // Clear form after success
            fullNameField.setText("");
            phoneField.setText("");
            departmentCombo.setSelectedIndex(0);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering walk-in: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
