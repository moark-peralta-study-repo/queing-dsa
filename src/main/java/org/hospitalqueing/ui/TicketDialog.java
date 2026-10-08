package org.hospitalqueing.ui;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import net.miginfocom.swing.MigLayout;

/**
 * A modal "ticket" dialog: big queue number, a real scannable QR of the ticket token, and a few
 * detail lines. Replaces the old plain-text popups in the patient queue + staff walk-in flows so
 * the waiting-room display / counter can actually scan the ticket.
 */
public final class TicketDialog {

  private TicketDialog() {}

  /**
   * @param parent      component the dialog is centered over (owner)
   * @param intro       lead-in line, e.g. "You have successfully joined the queue!"
   * @param department  department name
   * @param queueNumber the assigned queue number, shown as a department-prefixed ticket (e.g. "A-023")
   * @param qrToken     the ticket token — encoded to a QR image and also shown as text
   * @param extra       optional extra detail lines (joined with newlines); may be null
   */
  public static void show(Component parent, String intro, String department, int queueNumber, String qrToken, String extra) {
    int qrSize = 220;
    java.awt.image.BufferedImage qr = QrUtils.toImage(qrToken, qrSize);

    JDialog dialog = new JDialog((javax.swing.JFrame) (parent instanceof javax.swing.JFrame ? parent : findFrame(parent)), "Your Ticket", true);
    dialog.setBackground(Color.WHITE);
    dialog.setLayout(new BorderLayout());

    JPanel card = new JPanel(new MigLayout("wrap 1, insets 28 36 28 36, fillx, alignx center", "[center]", "[]12[]14[]20[]12[]"));
    card.setBackground(Color.WHITE);

    JLabel introLbl = new JLabel(intro == null ? "" : intro);
    introLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
    introLbl.setForeground(new Color(113, 128, 150));

    JLabel numLbl = new JLabel(UiData.queueLabel(department, queueNumber));
    numLbl.setFont(new Font("SansSerif", Font.BOLD, 60));
    numLbl.setForeground(new Color(21, 101, 192));

    JLabel qrLbl = new JLabel(new ImageIcon(qr), SwingConstants.CENTER);
    qrLbl.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 235), 1, true));
    qrLbl.setPreferredSize(new Dimension(qrSize, qrSize));

    JLabel deptLbl = new JLabel("Department: " + (department == null ? "" : department));
    deptLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
    deptLbl.setForeground(new Color(45, 55, 72));

    JLabel tokenLbl = new JLabel("Token: " + qrToken);
    tokenLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
    tokenLbl.setForeground(new Color(113, 128, 150));

    JLabel extraLbl = null;
    if (extra != null && !extra.isBlank()) {
      extraLbl = new JLabel(extra);
      extraLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
      extraLbl.setForeground(new Color(45, 55, 72));
      extraLbl.setHorizontalAlignment(SwingConstants.CENTER);
    }

    card.add(introLbl);
    card.add(numLbl);
    card.add(qrLbl);
    card.add(deptLbl);
    card.add(tokenLbl);
    if (extraLbl != null) {
      card.add(extraLbl);
    }

    JButton ok = new JButton("OK");
    ok.setBackground(new Color(21, 101, 192));
    ok.setForeground(Color.WHITE);
    ok.setFocusPainted(false);
    ok.setBorderPainted(false);
    ok.setFont(new Font("SansSerif", Font.BOLD, 13));
    ok.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    ok.setPreferredSize(new Dimension(120, 40));
    ok.addActionListener(e -> dialog.dispose());
    card.add(ok, "gaptop 26");

    dialog.add(card, BorderLayout.CENTER);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }

  private static javax.swing.JFrame findFrame(Component c) {
    Component cur = c;
    while (cur != null) {
      if (cur instanceof javax.swing.JFrame f) {
        return f;
      }
      cur = cur.getParent();
    }
    return null; // unowned modal dialog
  }
}
