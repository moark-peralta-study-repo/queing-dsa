package org.hospitalqueing.ui;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.Component;

/**
 * Helper so tables that keep real {@link JButton}s in their model cells (the admin add/delete
 * tables) render them as live buttons instead of the default renderer's
 * {@code toString()} (which shows {@code javax.swing.JButton[...]}).
 *
 * <p>Usage: right after building the table, {@code TableButtons.renderButtons(table, 5)} — the
 * button is placed as the cell component, so its own action listeners keep working.
 */
public final class TableButtons {

  private TableButtons() {}

  /** Applies a button-aware renderer to every column that may hold a {@link JButton} cell. */
  public static void renderButtons(JTable table) {
    apply(table, 0, table.getColumnCount());
  }

  /** Applies a button-aware renderer to a single column (0-based). */
  public static void renderButtons(JTable table, int column) {
    apply(table, column, column + 1);
  }

  private static void apply(JTable table, int from, int to) {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
          boolean focused, int row, int col) {
        if (value instanceof JButton) {
          return (JButton) value;
        }
        return super.getTableCellRendererComponent(t, value, selected, focused, row, col);
      }
    };
    TableColumnModel model = table.getColumnModel();
    for (int c = from; c < to && c < model.getColumnCount(); c++) {
      model.getColumn(c).setCellRenderer(renderer);
    }
  }
}
