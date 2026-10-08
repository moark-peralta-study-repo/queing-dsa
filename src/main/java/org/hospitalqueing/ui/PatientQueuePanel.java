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
import org.hospitalqueing.model.User;
import org.hospitalqueing.service.QueueManagementService;

public class PatientQueuePanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color TEXT_DARK = new Color(45, 55, 72);
    private final Color WHITE = Color.WHITE;

    private MainFrame parentFrame;
    private JLabel backBtn;
    private JComboBox<String> departmentCombo;
    private JTextArea notesArea;
    private JButton joinQueueBtn;

    public PatientQueuePanel(MainFrame parentFrame) {
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

        departmentCombo = new JComboBox<>(buildDepartmentOptions());
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

        joinQueueBtn.addActionListener(e -> joinQueue());
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

    private void joinQueue() {
        String department = (String) departmentCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        if (department == null || department.equals("Select Department...")) {
            JOptionPane.showMessageDialog(this, "Please select a department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User loggedIn = parentFrame.getLoggedInUser();
            int patientId = UiData.patientIdForUser(loggedIn);
            if (patientId < 0) {
                throw new Exception("No patient profile found for your account. Please register again from the home page.");
            }
            int departmentId = UiData.departmentIdByName(department);
            if (departmentId < 0) {
                throw new Exception("Department \"" + department + "\" not found. Please pick another.");
            }
            int serviceId = UiData.serviceIdForDepartmentId(departmentId);
            if (serviceId < 0) {
                throw new Exception("Department \"" + department + "\" has no active service. Please pick another.");
            }

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
                    "You have successfully joined the queue!",
                    department,
                    saved.getQueueNumber(),
                    saved.getQrToken(),
                    "Please wait for your number to be called.");

            departmentCombo.setSelectedIndex(0);
            notesArea.setText("");
            parentFrame.showScreen("DASHBOARD_PAGE");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error joining queue: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Preloads the dropdown menu from the dashboard click ---
    public void preselectDepartment(String departmentName) {
        if (departmentCombo != null) {
            departmentCombo.setSelectedItem(departmentName);
        }
    }
}
