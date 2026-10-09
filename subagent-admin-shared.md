# Shared context — queing-dsa admin expansion (3 parallel subagents)

## Repo / build
- Main repo: `/root/dev/enyu/queing-dsa`. **You work ONLY in your assigned worktree** (given in your task).
- Java 21, Gradle at `/opt/gradle-9.6.1/bin/gradle`. Build: `gradle -q compileJava`.
- UI is Swing: MigLayout + FlatLaf. Dark headers `new Color(13,37,63)`, primary blue `new Color(21,101,192)`, light bg `new Color(245,247,250)` / `(240,244,248)`.
- Verify under xvfb: `xvfb-run -a java -cp <runtime-cp> YourSmoke` (main-thread smoke classes, `System.exit(0/1)` at end). Runtime CP: `/opt/gradle-9.6.1/bin/gradle -q printRuntimeCp` prints it (or glob `$(find /root/.gradle/caches/modules-2 -name '*.jar' | tr '\n' ':')`).
- Login seeds: patient/patient, staff/staff, admin/admin, dr.santos/dr.santos.
- DB: `hospital.db` at worktree root (relative to cwd). Init: `DatabaseConnection.initializeDatabase(); SeedDemoData.seedIfEmpty();`.
- **Delete smoke .java + hospital.db before finishing.** Commit with your task's message. Do NOT push, do NOT create PRs — the parent merges.

## File ownership (STRICT — other subagents own the rest)
Shared helpers you may READ but not edit: `MainFrame.java`, `AdminDashboardPanel.java`, `UiData.java`, `BackButtons.java`, `WaitingRoomPanel.java`.

### Back buttons (use the shared helper, T-admin-req-5)
```java
import org.hospitalqueing.ui.BackButtons;
// in your panel header (dark bar):
headerPanel.add(BackButtons.back(parentFrame, "ADMIN_DASHBOARD"));
```
`MainFrame.showScreen("ADMIN_DASHBOARD")` routes to the admin dashboard (lands on its home). `parentFrame` is the `MainFrame` your constructor receives (all admin sub-panels get it: `new AdminXxxPanel(MainFrame parentFrame)`).

## Existing admin patterns to mirror
- `AdminDashboardPanel` — dark nav bar + internal `CardLayout` (`adminContentPanel`). Cards registered by string name; `showAdmin(String)` switches. The PARENT adds your card + nav link; you only build the panel class.
- `AdminDoctorsPanel` / `AdminStaffPanel` — add-entity form on top, table below, Delete button renderer guarded by "In use" (e.g. doctor with open appointments). Mirror that style.
- `StaffDashboardPanel.refreshHomeStats()` — computes live Today's Patients / In Queue / Completed for a department. Reuse the same queries (via `QueueEntryDAO`, `QueueManagementService`, `UiData`) scoped to your department.
- `WaitingRoomPanel` — Now-Serving label via `UiData.queueLabel(deptName, number)`, live auto-poll (Swing `Timer` 3000 ms), `qms.getActiveQueue(deptId)`.
- Trash bin (T10) — `TrashBinPanel` + `AppointmentDAO`/`QueueEntryDAO` `softDelete/restore/permanentlyDelete/findTrashed`; `deleted_at TEXT` columns. The Accounts subagent extends this pattern to `users` + `patients` (see task).
- `UserDAO` — `findAll`, `update`, `delete`, `findById`, `findByUsername`. `User` model: userId, username, passwordHash, roleId, isActive, createdAt. Roles: 1 PATIENT, 2 STAFF, 3 DEPARTMENT_STAFF, 4 ADMIN, 5 DOCTOR (`UiData.roleIdByName` / `roleNameById` if present, else query `roles`).
- `PatientDAO` — `findByUserId(userId)` returns the patient profile linked to a user (or null). `update(patient)` exists.

## UI conventions
- Tables: `DefaultTableModel` with `isCellEditable=false`, row height 35-38, action buttons via shared `JButton` + `DefaultTableCellRenderer` (see AppointmentHistoryPanel delete column, TrashBinPanel restore/permanent columns).
- Dialogs for edits: `JDialog` with MigLayout, OK/Cancel buttons; confirm destructive ops with `JOptionPane.showConfirmDialog`.
- Every panel: `loadXxxData()` refresh method that the parent/updater calls when shown.

## Done-criteria
- Compiles clean in your worktree.
- xvfb smoke verifies the core flow (see your task's verify list) — report check counts in your summary.
- One commit on your branch; tree clean (only your owned files changed).
