package org.hospitalqueing.ui;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Shared "&lt; Back" back-to-dashboard button (admin expansion). Every admin sub-page mounts one
 * so the user can always return to the admin dashboard with a single click.
 *
 * <p>Usage: {@code headerPanel.add(BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD"), ...)}
 * The {@code navigate} function receives the screen-name string the caller passes as
 * {@code targetScreen}; typically it's {@code MainFrame::showScreen}.
 */
public final class BackButtons {

  private BackButtons() {}

  /**
   * A clickable "&lt; Back" label that calls {@code navigate.accept(targetScreen)} on click.
   */
  public static JLabel back(Consumer<String> navigate, String targetScreen) {
    JLabel label = new JLabel("<  Back");
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    label.setForeground(Color.WHITE);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        navigate.accept(targetScreen);
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
}
