package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class HomePagePanel extends JPanel {

    private final Color PRIMARY_BLUE = new Color(21, 101, 192);   
    private final Color LIGHT_BLUE = new Color(227, 242, 253);    
    private final Color TEXT_MUTED = new Color(113, 128, 150);    
    private final Color WHITE = Color.WHITE;

    private JButton bottomLoginBtn;
    private JButton createAccBtn;
    
    private JScrollPane scrollPane;
    private JPanel scrollContentPanel;
    
    // Anchor points for scrolling
    private JPanel bannerPanel;
    private JLabel servicesTitle;
    private JLabel doctorsTitle;
    private JLabel aboutTitle;

    public HomePagePanel() {
        setLayout(new BorderLayout());
        setBackground(WHITE);

        // Main content wrapper
        scrollContentPanel = new JPanel(new MigLayout("wrap, fillx, insets 0", "[grow, fill]", "[]"));
        scrollContentPanel.setBackground(WHITE);

        // --- 1. HOME BANNER SECTION ---
        bannerPanel = new JPanel(new GridBagLayout());
        bannerPanel.setPreferredSize(new Dimension(800, 250));
        bannerPanel.setBackground(LIGHT_BLUE); 
        JLabel bannerText = new JLabel("Welcome to [Hospital Name]");
        bannerText.setFont(new Font("SansSerif", Font.BOLD, 32));
        bannerText.setForeground(PRIMARY_BLUE);
        bannerPanel.add(bannerText);
        scrollContentPanel.add(bannerPanel);

        // Information Wrapper
        JPanel infoSection = new JPanel(new MigLayout("wrap, fillx, insets 40 50 40 50", "[grow, fill]", "[]20[]"));
        infoSection.setBackground(WHITE);

        // --- 2. SERVICES SECTION ---
        servicesTitle = createSectionTitle("Our Core Services");
        infoSection.add(servicesTitle);
        infoSection.add(createBodyText(
            "• Emergency & Trauma Care\n" +
            "• Outpatient Consultation & Diagnostics\n" +
            "• Specialized Surgery & Rehabilitation\n" +
            "• Smart Queuing & Fast-Track Pharmacy"
        ));

        // --- 3. DOCTORS SECTION ---
        doctorsTitle = createSectionTitle("Meet Our Specialists");
        infoSection.add(doctorsTitle, "gaptop 40");
        infoSection.add(createBodyText(
            "Our hospital is home to top-tier medical professionals dedicated to your recovery.\n\n" +
            "• Dr. Jane Doe - Head of Cardiology\n" +
            "• Dr. John Smith - Lead Pediatrician\n" +
            "• Dr. Emily Chen - General Surgery"
        ));

        // --- 4. ABOUT SECTION ---
        aboutTitle = createSectionTitle("About Our Hospital");
        infoSection.add(aboutTitle, "gaptop 40");
        infoSection.add(createBodyText(
            "Founded in 2024, our facility integrates cutting-edge technology with compassionate care. " +
            "Our smart queueing system ensures you spend less time waiting and more time healing.\n\n" +
            "Operating Hours:\n" +
            "Emergency Room: 24/7 Open\n" +
            "Outpatient Clinics: Mon - Sat, 8:00 AM - 5:00 PM\n" +
            "Phone: (123) 456-7890"
        ));

        scrollContentPanel.add(infoSection);

        // Bottom Action Area
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30));
        bottomPanel.setBackground(WHITE);

        bottomLoginBtn = new JButton("Log in");
        createAccBtn = new JButton("Create Account");
        stylePrimaryButton(bottomLoginBtn);
        styleSecondaryButton(createAccBtn);

        bottomPanel.add(bottomLoginBtn);
        bottomPanel.add(createAccBtn);
        scrollContentPanel.add(bottomPanel);

        // Scroll Pane Setup
        scrollPane = new JScrollPane(scrollContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // Hides scrollbar visually

        add(scrollPane, BorderLayout.CENTER);
    }

    // --- SCROLLING LOGIC ---
    public void scrollToTop() { scrollToComponent(bannerPanel); }
    public void scrollToServices() { scrollToComponent(servicesTitle); }
    public void scrollToDoctors() { scrollToComponent(doctorsTitle); }
    public void scrollToAbout() { scrollToComponent(aboutTitle); }

    private void scrollToComponent(Component target) {
        SwingUtilities.invokeLater(() -> {
            // Accurately calculates the Y position of the target relative to the scroll container
            int targetY = SwingUtilities.convertPoint(target, 0, 0, scrollContentPanel).y;
            scrollPane.getVerticalScrollBar().setValue(targetY);
        });
    }

    // --- UI HELPERS ---
    private JLabel createSectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(PRIMARY_BLUE);
        return title;
    }

    private JTextArea createBodyText(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
        textArea.setForeground(TEXT_MUTED);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        return textArea;
    }

    private void stylePrimaryButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(WHITE);
        button.setForeground(PRIMARY_BLUE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    public JButton getBottomLoginBtn() { return bottomLoginBtn; }
    public JButton getCreateAccBtn() { return createAccBtn; }
}