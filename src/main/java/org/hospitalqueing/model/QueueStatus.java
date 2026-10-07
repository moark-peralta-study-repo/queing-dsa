package org.hospitalqueing.model;

import java.util.Set;

/**
 * Canonical status vocabulary for {@code queue_entries.status}.
 *
 * <p>These are the single source of truth for every queue status string. The DB CHECK
 * constraint, {@code QueueManagementService}, the web API, the Flutter ticket screen and the
 * Swing staff/patient panels must all use these values so a ticket's status never drifts between
 * the UI and the database.
 *
 * <p>The set mirrors the clinical workflow shown in the staff "Patient Controls" panel:
 * a ticket starts {@code Waiting}, is called and becomes {@code Checked In}, then
 * {@code In Consultation}; staff may detour it to {@code For Laboratory}/{@code For Pharmacy};
 * it ends as {@code Discharged}, {@code Completed} or {@code No Show}.
 */
public final class QueueStatus {
  public static final String WAITING = "Waiting";
  public static final String CHECKED_IN = "Checked In";
  public static final String IN_CONSULTATION = "In Consultation";
  public static final String FOR_LABORATORY = "For Laboratory";
  public static final String FOR_PHARMACY = "For Pharmacy";
  public static final String DISCHARGED = "Discharged";
  public static final String COMPLETED = "Completed";
  public static final String NO_SHOW = "No Show";

  /** Still in the active queue (shown by the staff live-queue table). */
  private static final Set<String> ACTIVE =
      Set.of(WAITING, CHECKED_IN, IN_CONSULTATION, FOR_LABORATORY, FOR_PHARMACY);
  /** Terminal states (removed from the active queue). */
  private static final Set<String> TERMINAL = Set.of(DISCHARGED, COMPLETED, NO_SHOW);
  /** Currently at a counter (drives the "Now serving" number). */
  private static final Set<String> AT_COUNTER = Set.of(CHECKED_IN, IN_CONSULTATION);

  /** Every legal value, used to validate manual "Update Status" writes. */
  private static final Set<String> ALL;
  static {
    java.util.HashSet<String> s = new java.util.HashSet<>(ACTIVE);
    s.addAll(TERMINAL);
    ALL = Set.copyOf(s);
  }

  private QueueStatus() {}

  public static boolean isAllowed(String status) {
    return status != null && ALL.contains(status);
  }

  public static boolean isActive(String status) {
    return status != null && ACTIVE.contains(status);
  }

  public static boolean isTerminal(String status) {
    return status != null && TERMINAL.contains(status);
  }

  public static boolean isAtCounter(String status) {
    return status != null && AT_COUNTER.contains(status);
  }
}
