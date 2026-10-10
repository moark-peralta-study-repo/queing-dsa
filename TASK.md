# TASK: System Settings panel + Admin dashboard stat cards
Worktree: /root/dev/enyu/qdsa-settings  Branch: feat/admin-settings
Read /root/dev/enyu/queing-dsa/subagent-security-shared.md FIRST.

OWNED FILES (only these + smoke):
1. ui/AdminSystemSettingsPanel.java — ctor(MainFrame). Dark header + BackButtons.back(parentFrame::showScreen,"ADMIN_DASHBOARD") + "SYSTEM SETTINGS". MigLayout form of known keys, each a labeled JTextField: cctv_url_1..cctv_url_4 (for the CCTV monitor panel), backup_dir (informational, defaults "backups"), maintenance_mode (ON/OFF combo — stored in system_settings; no behavior yet, note "planned"). [ SAVE ] button persists all fields via raw JDBC upserts: try { INSERT INTO system_settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value } catch(Throwable t){ show "settings table not available yet" dialog; } — this way you NEVER depend on SettingsDAO (another subagent owns it and may land after you). On construction, prefill fields from the table (same raw-JDBC-with-catch pattern).
2. ui/AdminOverviewCards.java — a small helper panel (no ctor arg or ctor(MainFrame)) exposing:
   - long countPatients(), long countActiveUsers(), long countOpenIncidents() (all raw JDBC counts, each in try/catch returning -1 on error; the incidents table may not exist yet — that's fine),
   - JPanel getOverviewPanel() — 3 stat cards (TOTAL PATIENTS / ACTIVE USERS / UPTIME) using the same card style as AdminDashboardPanel's refreshStatsCards (read that method for the visual pattern), values from the methods above, "— " for -1. Uptime = now - app start (use a static long stamped in the class initializer — close enough for a demo).
Smoke (in worktree): construct the settings panel; create the table via raw JDBC if missing; put cctv_url_1='http://cam1' through JDBC; rebuild panel -> field shows 'http://cam1'; save changes another key -> JDBC read-back correct; overview panel: countPatients() > 0 on seeded DB, countOpenIncidents() == -1 (no incidents table yet) or >= 0 if it exists. PASS/FAIL + RESULT.
Compile clean; delete smoke + hospital.db; commit: 'feat(admin): system settings panel + overview stat cards'. Do NOT push/PR.
