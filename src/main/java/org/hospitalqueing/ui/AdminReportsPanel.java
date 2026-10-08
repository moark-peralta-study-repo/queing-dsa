package org.hospitalqueing.ui;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hospitalqueing.dao.AppointmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Appointment;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.Service;

/**
 * Admin section (T9): a read-only reports & analytics view.
 *
 * <p>Shows facility totals (appointments, walk-ins/queue entries, patients, doctors), a
 * per-department breakdown of appointments + walk-ins, last-7-days daily activity (appointments
 * created + queue joins per day), and the appointment completion rate (completed over total by
 * status, with a per-status count). All numbers are read straight from the DAOs — nothing on this
 * panel mutates data.
 */
public class AdminReportsPanel extends JPanel {

  private final MainFrame parentFrame;

  private final Color BACKGROUND_LIGHT = new Color(245, 247, 250);
  private final Color TEXT_DARK = new Color(45, 55, 72);
  private final Color TEXT_MUTED = new Color(113, 128, 150);
  private final Color PRIMARY_BLUE = new Color(21, 101, 192);
  private final Color CARD_BORDER = new Color(225, 230, 235);

  // Total value labels (updated by refresh()).
  private final JLabel appointmentsCardValue;
  private final JLabel walkInsCardValue;
  private final JLabel patientsCardValue;
  private final JLabel doctorsCardValue;

  // Breakdown tables (read-only).
  private final DefaultTableModel deptModel =
      readonlyModel(new Object[]{"Department", "Appointments", "Walk-ins"});
  private final DefaultTableModel dailyModel =
      readonlyModel(new Object[]{"Date", "Appointments Created", "Queue Joins"});

  // Completion-rate display.
  private JLabel completionValueLbl;
  private JLabel completionDetailLbl;
  private final Map<String, JLabel> statusLabels = new LinkedHashMap<>();

  public AdminReportsPanel(MainFrame parentFrame) {
    this.parentFrame = parentFrame;
    setLayout(new BorderLayout());
    setBackground(BACKGROUND_LIGHT);

    JPanel scrollArea =
        new JPanel(new MigLayout("insets 30 40 30 40, fillx, wrap 1", "[grow, fill]", "[]14[]14[]14[]14[]14[]"));
    scrollArea.setOpaque(false);

    // --- Title ---
    JPanel titlePanel = new JPanel(new MigLayout("insets 0", "[left]", "[]4[]"));
    titlePanel.setOpaque(false);
    JLabel title = new JLabel("Reports & Analytics");
    title.setFont(new Font("SansSerif", Font.BOLD, 22));
    title.setForeground(TEXT_DARK);
    JLabel subtitle = new JLabel("Read-only overview of facility activity");
    subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
    subtitle.setForeground(TEXT_MUTED);
    titlePanel.add(title, "wrap");
    titlePanel.add(subtitle);
    scrollArea.add(titlePanel);

    // --- A. Facility totals ---
    scrollArea.add(createSectionHeader("Facility Totals"));
    JPanel totals =
        new JPanel(new MigLayout("insets 0, gap 16", "[grow, fill][grow, fill][grow, fill][grow, fill]", "[]"));
    totals.setOpaque(false);
    appointmentsCardValue = styleValueLabel();
    walkInsCardValue = styleValueLabel();
    patientsCardValue = styleValueLabel();
    doctorsCardValue = styleValueLabel();
    totals.add(statCard("Appointments", "📅", new Color(240, 235, 255), new Color(123, 92, 227), appointmentsCardValue));
    totals.add(statCard("Walk-ins", "🎫", new Color(230, 244, 255), PRIMARY_BLUE, walkInsCardValue));
    totals.add(statCard("Patients", "🧑", new Color(235, 249, 241), new Color(46, 204, 113), patientsCardValue));
    totals.add(statCard("Doctors", "🩺", new Color(255, 244, 229), new Color(230, 126, 34), doctorsCardValue));
    scrollArea.add(totals);

    // --- B. Per-department breakdown ---
    scrollArea.add(createSectionHeader("Per-Department Breakdown"));
    scrollArea.add(tableCard(new JTable(deptModel)));

    // --- C. Last 7 days ---
    scrollArea.add(createSectionHeader("Last 7 Days Activity"));
    scrollArea.add(tableCard(new JTable(dailyModel)));

    // --- D. Appointment completion rate ---
    scrollArea.add(createSectionHeader("Appointment Completion Rate"));
    scrollArea.add(buildCompletionCard());

    add(new JScrollPane(scrollArea), BorderLayout.CENTER);

    refresh();
  }

  /** Re-queries all report data from the DAOs. Called on construction and whenever the section is shown. */
  public void refresh() {
    List<Appointment> appointments = safeList(() -> new AppointmentDAO().findAll());
    List<QueueEntry> entries = safeList(() -> new QueueEntryDAO().findAll());
    List<Department> departments = safeList(UiData::activeDepartments);

    int totalPatients = safeInt(() -> new PatientDAO().findAll().size());
    int totalDoctors = safeInt(() -> new DoctorDAO().findAll().size());

    appointmentsCardValue.setText(String.valueOf(appointments.size()));
    walkInsCardValue.setText(String.valueOf(entries.size()));
    patientsCardValue.setText(String.valueOf(totalPatients));
    doctorsCardValue.setText(String.valueOf(totalDoctors));

    // Per-department: appointments are department-scoped via their service, then doctor (fallback).
    deptModel.setRowCount(0);
    int apptTotal = 0;
    int walkTotal = 0;
    for (Department dept : departments) {
      int deptId = dept.getDepartmentId();
      int appts = 0;
      int walks = 0;
      for (Appointment a : appointments) {
        if (deptIdForAppointment(a) == deptId) {
          appts++;
        }
      }
      for (QueueEntry e : entries) {
        if (e.getDepartmentId() == deptId) {
          walks++;
        }
      }
      deptModel.addRow(new Object[]{dept.getDepartmentName(), appts, walks});
      apptTotal += appts;
      walkTotal += walks;
    }
    deptModel.addRow(new Object[]{"Total", apptTotal, walkTotal});

    // Last 7 days: appointments created (created_at date) + queue joins (joined_at date) per day.
    LocalDate today = LocalDate.now();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE d MMM");
    dailyModel.setRowCount(0);
    for (int i = 6; i >= 0; i--) {
      LocalDate day = today.minusDays(i);
      int apptDay = 0;
      int joinDay = 0;
      for (Appointment a : appointments) {
        if (a.getCreatedAt() != null && a.getCreatedAt().toLocalDate().equals(day)) {
          apptDay++;
        }
      }
      for (QueueEntry e : entries) {
        LocalDate joined = parseDate(e.getJoinedAt());
        if (joined != null && joined.equals(day)) {
          joinDay++;
        }
      }
      dailyModel.addRow(new Object[]{day.format(fmt), apptDay, joinDay});
    }

    // Completion rate: completed over total by status.
    int completed = 0;
    int total = appointments.size();
    statusLabels.values().forEach(l -> l.setText("0"));
    for (Appointment a : appointments) {
      String st = a.getStatus() == null ? "" : a.getStatus().toUpperCase();
      if ("COMPLETED".equals(st)) {
        completed++;
      }
      JLabel lbl = statusLabels.get(st);
      if (lbl != null) {
        lbl.setText(String.valueOf(Integer.parseInt(lbl.getText()) + 1));
      }
    }
    int pct = total > 0 ? (int) Math.round(completed * 100.0 / total) : 0;
    completionValueLbl.setText(pct + "%");
    completionDetailLbl.setText(completed + " of " + total + " appointments completed");
  }

  // --- layout builders ---

  private JLabel createSectionHeader(String text) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("SansSerif", Font.BOLD, 16));
    l.setForeground(TEXT_DARK);
    return l;
  }

  private static DefaultTableModel readonlyModel(Object[] columns) {
    return new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }

  private JLabel styleValueLabel() {
    JLabel l = new JLabel("0");
    l.setFont(new Font("SansSerif", Font.BOLD, 30));
    l.setForeground(TEXT_DARK);
    return l;
  }

  private JPanel statCard(String title, String icon, Color bg, Color iconColor, JLabel value) {
    JPanel card = new JPanel(new MigLayout("insets 16, fillx", "[left]push[right]", "[]8[]"));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
    titleLbl.setForeground(TEXT_MUTED);
    JLabel iconLbl = new JLabel(icon);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
    iconLbl.setForeground(iconColor);
    card.add(titleLbl, "cell 0 0");
    card.add(iconLbl, "cell 1 0");
    card.add(value, "cell 0 1, span 2");
    return card;
  }

  private JComponent tableCard(JTable table) {
    table.setFont(new Font("SansSerif", Font.PLAIN, 13));
    table.setBackground(Color.WHITE);
    table.setRowHeight(26);
    table.setIntercellSpacing(new Dimension(1, 2));
    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
    return scroll;
  }

  private JPanel buildCompletionCard() {
    JPanel card = new JPanel(new MigLayout("insets 24, fillx, gapx 30", "[left]push[right]", "[][grow]"));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));

    // Left: big percentage + detail.
    completionValueLbl = new JLabel("0%");
    completionValueLbl.setFont(new Font("SansSerif", Font.BOLD, 44));
    completionValueLbl.setForeground(PRIMARY_BLUE);
    JLabel capLbl = new JLabel("Completion Rate");
    capLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
    capLbl.setForeground(TEXT_MUTED);
    completionDetailLbl = new JLabel("0 of 0 appointments completed");
    completionDetailLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
    completionDetailLbl.setForeground(TEXT_MUTED);
    JPanel left = new JPanel(new MigLayout("insets 0", "[left]", "[]6[]"));
    left.setOpaque(false);
    left.add(completionValueLbl, "wrap");
    left.add(capLbl, "wrap");
    left.add(completionDetailLbl);
    card.add(left, "cell 0 0, span 2, align left");

    // Right: per-status counts ("by status").
    JPanel right = new JPanel(new MigLayout("insets 0", "[grow, fill]", "[]6[]"));
    right.setOpaque(false);
    for (String st : new String[]{"SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW"}) {
      JLabel nameLbl = new JLabel(st.charAt(0) + st.substring(1).toLowerCase().replace('_', ' '));
      nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
      nameLbl.setForeground(TEXT_DARK);
      JLabel countLbl = new JLabel("0");
      countLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
      countLbl.setForeground(TEXT_MUTED);
      JPanel row = new JPanel(new BorderLayout());
      row.setOpaque(false);
      row.add(nameLbl, BorderLayout.WEST);
      row.add(countLbl, BorderLayout.EAST);
      right.add(row);
      statusLabels.put(st, countLbl);
    }
    card.add(right, "cell 1 1, align right");

    return card;
  }

  // --- data helpers ---

  /** Department for an appointment: via its service (primary), else its doctor (fallback), else -1. */
  private int deptIdForAppointment(Appointment a) {
    int sid = a.getServiceId();
    if (sid > 0) {
      Service s = safe(() -> new ServiceDAO().findById(sid), null);
      if (s != null) {
        return s.getDepartmentId();
      }
    }
    Integer did = a.getDoctorId();
    if (did != null && did > 0) {
      Doctor d = safe(() -> new DoctorDAO().findById(did), null);
      if (d != null) {
        return d.getDepartmentId();
      }
    }
    return -1;
  }

  /** First 10 chars of a "yyyy-MM-dd HH:mm:ss" timestamp as a LocalDate, or null when unparseable. */
  private static LocalDate parseDate(String s) {
    if (s == null || s.length() < 10) {
      return null;
    }
    try {
      return LocalDate.parse(s.substring(0, 10));
    } catch (Exception e) {
      return null;
    }
  }

  private <T> T safe(java.util.function.Supplier<T> s, T fallback) {
    try {
      return s.get();
    } catch (Exception e) {
      return fallback;
    }
  }

  private int safeInt(java.util.function.IntSupplier s) {
    try {
      return s.getAsInt();
    } catch (Exception e) {
      return 0;
    }
  }

  private <T> List<T> safeList(java.util.function.Supplier<List<T>> s) {
    try {
      List<T> r = s.get();
      return r == null ? new ArrayList<>() : r;
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }
}
