package org.hospitalqueing.ui;

import java.util.List;

import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.RoleDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.Patient;
import org.hospitalqueing.model.Role;
import org.hospitalqueing.model.Service;
import org.hospitalqueing.model.User;

/**
 * UI-facing data access facade used by the Swing panels.
 *
 * <p>Centralises the "translate a UI choice into a real database id" logic that the panels used to
 * hard-code. Everything here reads straight from SQLite via the DAOs, so the patient dashboard,
 * walk-in form, appointment form and staff queue all operate on the same live data instead of
 * invented strings. Each method degrades gracefully (returns empty / -1 / null) when the relevant
 * table has no rows yet, so a fresh UI never throws just because the seed is absent.
 */
public final class UiData {

  private final DepartmentDAO departmentDAO = new DepartmentDAO();
  private final DoctorDAO doctorDAO = new DoctorDAO();
  private final ServiceDAO serviceDAO = new ServiceDAO();
  private final PatientDAO patientDAO = new PatientDAO();
  private final RoleDAO roleDAO = new RoleDAO();

  private UiData() {}

  // --- Departments ---

  /** All active departments, in id order. */
  public static List<Department> activeDepartments() {
    return new UiData().departmentDAO.findAll();
  }

  /** Department names for combo boxes. */
  public static String[] departmentNames() {
    return activeDepartments().stream().map(Department::getDepartmentName).toArray(String[]::new);
  }

  /** Resolves a department id by (case-insensitive) name, or -1 if not found. */
  public static int departmentIdByName(String name) {
    if (name == null) {
      return -1;
    }
    for (Department d : activeDepartments()) {
      if (d.getDepartmentName().equalsIgnoreCase(name)) {
        return d.getDepartmentId();
      }
    }
    return -1;
  }

  // --- Doctors ---

  /** All active doctors. */
  public static List<Doctor> allDoctors() {
    return new UiData().doctorDAO.findAll();
  }

  /** Doctors serving a department (empty list if the department id is unknown or has none). */
  public static List<Doctor> doctorsForDepartment(int departmentId) {
    DoctorDAO doctorDAO = new DoctorDAO();
    return doctorDAO.findByDepartment(departmentId);
  }

  /**
   * Builds combo-box doctor labels ("Dr. First Last — Department") for one department, in a stable
   * order matching {@link #doctorIdsForDepartment(String)}.
   */
  public static String[] doctorLabelsForDepartment(String departmentName) {
    int deptId = departmentIdByName(departmentName);
    List<Doctor> docs = doctorsForDepartment(deptId);
    String[] labels = new String[docs.size()];
    Department dept = findDepartment(deptId);
    String deptLabel = dept != null ? dept.getDepartmentName() : "General";
    for (int i = 0; i < docs.size(); i++) {
      Doctor d = docs.get(i);
      labels[i] = "Dr. " + d.getFirstName() + " " + d.getLastName() + " — " + deptLabel;
    }
    return labels;
  }

  /** Doctor ids in the same order as {@link #doctorLabelsForDepartment(String)}. */
  public static int[] doctorIdsForDepartment(String departmentName) {
    int deptId = departmentIdByName(departmentName);
    List<Doctor> docs = doctorsForDepartment(deptId);
    int[] ids = new int[docs.size()];
    for (int i = 0; i < docs.size(); i++) {
      ids[i] = docs.get(i).getDoctorId();
    }
    return ids;
  }

  // --- Services ---

  /** First active service id for a department (the NOT NULL service_id an appointment needs), or -1. */
  public static int serviceIdForDepartment(String departmentName) {
    return serviceIdForDepartmentId(departmentIdByName(departmentName));
  }

  public static int serviceIdForDepartmentId(int departmentId) {
    if (departmentId < 0) {
      return -1;
    }
    for (Service s : new UiData().serviceDAO.findAll()) {
      if (s.getDepartmentId() == departmentId && s.isActive()) {
        return s.getServiceId();
      }
    }
    return -1;
  }

  /** Service name for a service id (for display), or null. */
  public static String serviceName(int serviceId) {
    if (serviceId <= 0) {
      return null;
    }
    Service s = new UiData().serviceDAO.findById(serviceId);
    return s == null ? null : s.getServiceName();
  }

  /** Department name for a department id (for display), or null. */
  public static String departmentName(int departmentId) {
    if (departmentId <= 0) {
      return null;
    }
    Department d = new UiData().departmentDAO.findById(departmentId);
    return d == null ? null : d.getDepartmentName();
  }

  /** Department name derived from a service id (for display), or null. */
  public static String departmentNameForService(int serviceId) {
    Service s = new UiData().serviceDAO.findById(serviceId);
    if (s == null) {
      return null;
    }
    return departmentName(s.getDepartmentId());
  }

  /** "Dr. First Last" for a doctor id (for display), or null. */
  public static String doctorName(int doctorId) {
    if (doctorId <= 0) {
      return null;
    }
    Doctor d = new UiData().doctorDAO.findById(doctorId);
    if (d == null) {
      return null;
    }
    return "Dr. " + d.getFirstName() + " " + d.getLastName();
  }

  /** Index of an item in a combo (JComboBox has no getIndexOf), or -1 if absent. */
  public static int indexOfItem(javax.swing.JComboBox<?> combo, Object value) {
    if (combo == null || value == null) return -1;
    for (int i = 0; i < combo.getItemCount(); i++) {
      if (value.equals(combo.getItemAt(i))) return i;
    }
    return -1;
  }

  // --- Patient identity ---

  /**
   * Resolves the {@code patients.patient_id} for a logged-in user. This is the critical bridge the
   * UI was missing: appointments and queue entries are foreign-keyed to {@code patients}, not to
   * {@code users}, so a user must be mapped to their patient row before writing either. Returns -1
   * when the user has no patient profile yet.
   */
  public static int patientIdForUser(User user) {
    if (user == null) {
      return -1;
    }
    Patient p = new UiData().patientDAO.findByUserId(user.getUserId());
    return p == null ? -1 : p.getPatientId();
  }

  /** Human-readable patient name ("First Last") or "Patient #id" if the row is gone. */
  public static String patientName(int patientId) {
    Patient p = new UiData().patientDAO.findById(patientId);
    if (p == null) {
      return "Patient #" + patientId;
    }
    String name = (p.getFirstName() == null ? "" : p.getFirstName().trim())
        + " "
        + (p.getLastName() == null ? "" : p.getLastName().trim());
    return name.isBlank() ? "Patient #" + patientId : name.trim();
  }

  /** The full patient profile for a logged-in user (for Profile), or null. */
  public static Patient patientProfileForUser(User user) {
    if (user == null) {
      return null;
    }
    return new UiData().patientDAO.findByUserId(user.getUserId());
  }

  /**
   * Creates the user + patient rows that a walk-in needs (the schema requires {@code patient_id}
   * to be a real patient, and patients require a user). Returns the new {@code patient_id}.
   *
   * <p>Builds a unique username from the name + phone; if the patient is created, a corresponding
   * user account is added so the profile can be looked up later.
   */
  public static int createWalkInPatient(String fullName, String phone) {
    org.hospitalqueing.dao.UserDAO userDAO = new org.hospitalqueing.dao.UserDAO();
    org.hospitalqueing.service.AuthenticationService auth =
        new org.hospitalqueing.service.AuthenticationService(userDAO);
    org.hospitalqueing.service.UserService userService =
        new org.hospitalqueing.service.UserService(userDAO);
    org.hospitalqueing.controller.UserController uc =
        new org.hospitalqueing.controller.UserController(userService);

    String base = (fullName == null || fullName.isBlank() ? "walkin" : fullName.toLowerCase())
        .replaceAll("[^a-z0-9]", "");
    String phonePart = (phone == null ? "" : phone).replaceAll("\\D", "");
    String unique =
        (base.isEmpty() ? "walkin" : base) + (phonePart.isEmpty() ? "" : "-" + phonePart);

    // Ensure a unique username.
    String username = unique;
    int suffix = 1;
    while (userDAO.findByUsername(username) != null) {
      username = unique + "-" + suffix++;
    }

    User user = new User();
    user.setUsername(username);
    user.setPasswordHash(auth.hashPassword(username)); // walk-in demo password == username
    int patientRoleId = roleIdByName("PATIENT");
    user.setRoleId(patientRoleId > 0 ? patientRoleId : 3);
    user.setActive(true);
    uc.createUser(user);

    // Resolve the generated user id.
    User created = userDAO.findByUsername(username);
    if (created == null) {
      throw new IllegalStateException("Could not resolve walk-in user id");
    }

    Patient patient = new Patient();
    patient.setUserId(created.getUserId());
    String[] parts = (fullName == null ? "" : fullName).trim().split("\\s+");
    patient.setFirstName(parts.length > 0 ? parts[0] : "Walk-in");
    patient.setLastName(parts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : "");
    patient.setPhone(phone == null ? "" : phone);
    new UiData().patientDAO.save(patient);
    return patient.getPatientId();
  }

  // --- Roles ---

  /** The role id for a role name (case-insensitive), or -1 if the roles table is empty. */
  public static int roleIdByName(String name) {
    Role r = new UiData().roleDAO.findByName(name);
    return r == null ? -1 : r.getRoleId();
  }

  /** The role name for a user's role id, or null. */
  public static String roleNameForUser(User user) {
    if (user == null) {
      return null;
    }
    Role r = new UiData().roleDAO.findById(user.getRoleId());
    return r == null ? null : r.getRoleName();
  }

  /** True when the user's role is a staff/administrator role (not a patient). */
  public static boolean isStaff(User user) {
    String name = roleNameForUser(user);
    if (name == null) {
      return false;
    }
    String n = name.toUpperCase();
    return n.contains("STAFF") || n.contains("ADMIN");
  }

  /** True when the user's role is specifically an admin (the management dashboard gate). */
  public static boolean isAdmin(User user) {
    String name = roleNameForUser(user);
    return name != null && name.toUpperCase().contains("ADMIN");
  }

  // --- internals ---

  private static Department findDepartment(int departmentId) {
    for (Department d : activeDepartments()) {
      if (d.getDepartmentId() == departmentId) {
        return d;
      }
    }
    return null;
  }
}
