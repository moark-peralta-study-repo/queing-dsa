package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.StaffDAO;
import org.hospitalqueing.model.Staff;

public class StaffService {
  private final StaffDAO staffDAO;

  public StaffService(StaffDAO staffDAO) {
    this.staffDAO = staffDAO;
  }

  public void createStaff(Staff staff) {
    staffDAO.save(staff);
  }

  public Staff getStaffById(int staffId) {
    return staffDAO.findById(staffId);
  }

  public Staff getStaffByUser(int userId) {
    return staffDAO.findByUser(userId);
  }

  public List<Staff> getAllStaff() {
    return staffDAO.findAll();
  }

  public void updateStaff(Staff staff) {
    staffDAO.update(staff);
  }

  public void deleteStaff(int staffId) {
    staffDAO.delete(staffId);
  }
}
