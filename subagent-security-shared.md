# SHARED CONTEXT — admin security-flow screens (work off feat/flows-and-screen)
- Repo: /root/dev/enyu/queing-dsa (you work in YOUR OWN worktree at /root/dev/enyu/<dir>)
- Build: /opt/gradle-9.6.1/bin/gradle -q compileJava   (headless box; Swing via xvfb-run -a)
- Java 21, Javalin, SQLite (org.xerial), MigLayout + FlatLaf. Package: org.hospitalqueing
- Patterns (copy style, don't invent new): 
  - New admin section = class in src/main/java/org/hospitalqueing/ui/AdminXxxPanel.java,
    ctor `public AdminXxxPanel(MainFrame parentFrame)`, dark header `new Color(13,37,63)`
    + `org.hospitalqueing.ui.BackButtons.back(parentFrame::showScreen, "ADMIN_DASHBOARD")` 
    (BackButtons API: static JButton back(Consumer<String> navigate, String screenName); label "<  Back")
    + title JLabel (bold 20) on the left, optional actions on the right, MigLayout "insets 15 30 15 30".
  - DAOs: src/main/java/org/hospitalqueing/dao/XxxDAO.java, ctor takes DatabaseConnection.getSingleton();
    SQL via prepareStatement; map rows to models (org.hospitalqueing.model.*).
  - Schema + migrations: src/main/java/org/hospitalqueing/database/DatabaseConnection.java —
    add CREATE TABLE IF NOT EXISTS in the schema block AND a guarded `migrateXxx()` (ALTER TABLE in
    try/catch) called at init, because existing DBs lack the column.
  - Tables render as JTable with DefaultTableModel, non-editable (isCellEditable false), inside
    JScrollPane, striped look optional. Never leave a 0-column table.
- Login for smoke: admin/admin (or staff/staff). MainFrame flow: new MainFrame(); setVisible(true);
  showScreen("LOGIN_PAGE"); set usernameField/passwordField via reflection; click loginBtn on a
  thread; dismiss the success JDialog (click its buttons); then the ADMIN_DASHBOARD panel is findable
  via recursive component-type search.
- Smoke harness: write /root/dev/enyu/<dir>/SmokeXxx.java at repo root of YOUR worktree,
  compile: CP="build/classes/java/main:$(find /root/.gradle/caches/modules-2 -name '*.jar' | tr '\n' ':'):build/smoke"
  javac -cp "$CP" -d build/smoke SmokeXxx.java && xvfb-run -a java -cp "$CP" SmokeXxx
  It must print PASS/FAIL lines + "RESULT: n/n passed" and System.exit(fail==0?0:1).
  DELETE the smoke .java, build/smoke and hospital.db when done (git clean check: only your feature files).
- Commit only within YOUR owned files. Do NOT push, do NOT open PRs, do NOT edit MainFrame.java
  or AdminDashboardPanel.java (parent wires nav later).
- The app is sized to the full screen now (min 1920x1080); your panels must not assume an 800px width —
  use MigLayout grow/fill so tables and cards stretch.
