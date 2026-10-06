package org.hospitalqueing.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.Role;
import org.hospitalqueing.model.Service;
import org.hospitalqueing.model.User;
import org.hospitalqueing.service.AuthenticationService;
import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.DoctorDAO;
import org.hospitalqueing.dao.PatientDAO;
import org.hospitalqueing.dao.RoleDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.Counter;
import org.hospitalqueing.model.Doctor;
import org.hospitalqueing.model.Patient;
import org.hospitalqueing.model.Staff;

/**
 * Idempotent demo seed so the system works out of the box:
 *
 * <ul>
 *   <li>Roles: PATIENT, STAFF, DEPARTMENT_STAFF, ADMIN</li>
 *   <li>Users: {@code patient} / {@code patient} (PATIENT),
 *       {@code staff} / {@code staff} (STAFF),
 *       {@code admin} / {@code admin} (ADMIN) — all weak on purpose, it's a demo</li>
 *   <li>One patient, four departments (each with two services + two counters), one staff record</li>
 * </ul>
 *
 * <p>Only inserts when the relevant table is still empty, so re-running the app never duplicates
 * rows. Passwords go through {@link AuthenticationService#hashPassword} (salted SHA-256) so
 * {@code login} works with the same verifier production would use.
 */
public final class SeedDemoData {

  private final RoleDAO roleDAO = new RoleDAO();
  private final UserDAO userDAO = new UserDAO();
  private final PatientDAO patientDAO = new PatientDAO();
  private final DepartmentDAO departmentDAO = new DepartmentDAO();
  private final ServiceDAO serviceDAO = new ServiceDAO();
  private final CounterDAO counterDAO = new CounterDAO();
  private final StaffDAO staffDAO = new StaffDAO();
  private final DoctorDAO doctorDAO = new DoctorDAO();

  private SeedDemoData() {}

  public static void seedIfEmpty() {
    new SeedDemoData().seed();
  }

  private void seed() {
    if (empty("roles")) {
      roleDAO.save(new Role(0, "PATIENT"));
      roleDAO.save(new Role(0, "STAFF"));
      roleDAO.save(new Role(0, "DEPARTMENT_STAFF"));
      roleDAO.save(new Role(0, "ADMIN"));
    }

    if (empty("patients")) {
      User patientUser = new User();
      patientUser.setUsername("patient");
      patientUser.setPasswordHash(new AuthenticationService(userDAO).hashPassword("patient"));
      patientUser.setRoleId(roleId("PATIENT"));
      patientUser.setActive(true);
      userDAO.save(patientUser);

      Patient patient = new Patient();
      patient.setUserId(patientUser.getUserId());
      patient.setFirstName("Maria");
      patient.setLastName("Cruz");
      patient.setMiddleName(null);
      patient.setBirthDate("1985-04-12");
      patient.setSex("F");
      patient.setPhone("555-0100");
      patientDAO.save(patient);

      User staffUser = new User();
      staffUser.setUsername("staff");
      staffUser.setPasswordHash(new AuthenticationService(userDAO).hashPassword("staff"));
      staffUser.setRoleId(roleId("STAFF"));
      staffUser.setActive(true);
      userDAO.save(staffUser);

      User adminUser = new User();
      adminUser.setUsername("admin");
      adminUser.setPasswordHash(new AuthenticationService(userDAO).hashPassword("admin"));
      adminUser.setRoleId(roleId("ADMIN"));
      adminUser.setActive(true);
      userDAO.save(adminUser);
    }

    // Seed each demo department individually so an existing DB (e.g. only Cardiology)
    // picks up the others on the next run.
    seedDepartmentIfMissing("Cardiology", "Consultation", "Follow-up", "101", "102");
    seedDepartmentIfMissing("General Medicine", "OPD", "Minor Procedure", "103", "104");
    seedDepartmentIfMissing("Pediatrics", "Check-up", "Immunization", "105", "106");
    seedDepartmentIfMissing("Orthopedics", "Fracture Clinic", "Joint Consultation", "107", "108");

    // Give every department at least one doctor so the appointment panel's doctor dropdown is
    // populated. Idempotent: only adds when the department has no doctor yet.
    seedDoctorIfMissing("Cardiology", "Maria", "Santos");
    seedDoctorIfMissing("General Medicine", "Carlo", "Reyes");
    seedDoctorIfMissing("Pediatrics", "Ana", "Lim");
    seedDoctorIfMissing("Orthopedics", "John", "Smith");

    if (empty("staff")) {
      Department firstDept = departmentDAO.findAll().get(0);
      Staff staff = new Staff();
      staff.setUserId(userDAO.findByUsername("staff").getUserId());
      staff.setFirstName("Alex");
      staff.setLastName("Reyes");
      staff.setDepartmentId(firstDept.getDepartmentId());
      staffDAO.save(staff);

      System.out.println("Seeded demo data. Logins: patient/patient, staff/staff, admin/admin");
    }
  }

  private void seedDepartmentIfMissing(
      String name, String service1, String service2, String room1, String room2) {
    boolean exists =
        departmentDAO.findAll().stream().anyMatch(d -> d.getDepartmentName().equals(name));
    if (exists) {
      return;
    }
    Department dept = new Department(0, name, true);
    departmentDAO.save(dept);
    int id = dept.getDepartmentId();
    serviceDAO.save(new Service(0, id, service1, 10, true));
    serviceDAO.save(new Service(0, id, service2, 15, true));
    counterDAO.save(new Counter(0, id, "Counter 1", "Room " + room1, true));
    counterDAO.save(new Counter(0, id, "Counter 2", "Room " + room2, true));
  }

  private int roleId(String name) {
    return roleDAO.findByName(name).getRoleId();
  }

  private void seedDoctorIfMissing(String departmentName, String first, String last) {
    Department dept =
        departmentDAO.findAll().stream()
            .filter(d -> d.getDepartmentName().equals(departmentName))
            .findFirst()
            .orElse(null);
    if (dept == null) {
      return;
    }
    boolean hasDoctor = doctorDAO.findByDepartment(dept.getDepartmentId()).size() > 0;
    if (hasDoctor) {
      return;
    }
    Doctor doctor = new Doctor();
    doctor.setDepartmentId(dept.getDepartmentId());
    doctor.setFirstName(first);
    doctor.setLastName(last);
    doctor.setLicenseNum("LIC-" + (first + last).replaceAll("\\s", "").toUpperCase());
    doctor.setActive(true);
    doctorDAO.save(doctor);
  }

  private static boolean empty(String table) {
    try (Connection connection = DatabaseConnection.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
      return !rs.next() || rs.getInt(1) == 0;
    } catch (SQLException e) {
      // Table missing shouldn't happen post-initialize; treat as "not empty" and skip.
      return false;
    }
  }
}
