package org.hospitalqueing.ui;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared "< " back-to-dashboard button (T-admin). Every admin/staff/doctor/patient sub-page
 * mounts one so the user can always get back to the role's home screen with a single click.
 *
 * <p>Usage: {@code headerPanel.add(BackButtons.back(homeFrame, "ADMIN_HOME"), ...)}
 */
public final class BackButtons {

  private BackButtons() {}

  /**
   * A clickable "< Back" label that routes {@code homeFrame.showScreen(target)} — the role's
   * home/dashboard card (e.g. "ADMIN_HOME", "STAFF_HOME", "DOCTOR_DASHBOARD", "DASHBOARD_PAGE").
   */
  public static JLabel back(JFrameOwner homeFrame, String targetScreen) {
    JLabel label = new JLabel("<  Back");
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    label.setForeground(Color.WHITE);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        homeFrame.showScreen(targetScreen);
      }
      @Override
      public void mouseEntered(MouseEvent e) {
        label.setForeground(new Color(180, 200, 230));
      }
      @Override
      public void mouseExited(MouseEvent e) {
        label.setForeground(Color.WHITE);
      }
    });
    return label;
  }

  /**
   * Interface over MainFrame so this helper doesn't import the frame type directly (keeps the
   * sub-panel -> frame dependency one-directional via showScreen only).
   */
  public interface JFrameOwner {
    void showScreen(String screenName);
  }
}
