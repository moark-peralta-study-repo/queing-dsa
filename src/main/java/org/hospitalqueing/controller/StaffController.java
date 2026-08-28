package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Staff;
import org.hospitalqueing.service.StaffService;

public class StaffController {
  private final StaffService staffService;

  public StaffController(StaffService staffService) {
    this.staffService = staffService;
  }

  public void createStaff(Staff staff) {
    staffService.createStaff(staff);
  }

  public Staff getStaff(int staffId) {
    return staffService.getStaffById(staffId);
  }

  public Staff getStaffByUser(int userId) {
    return staffService.getStaffByUser(userId);
  }

  public List<Staff> getAllStaff() {
    return staffService.getAllStaff();
  }

  public void updateStaff(Staff staff) {
    staffService.updateStaff(staff);
  }

  public void deleteStaff(int staffId) {
    staffService.deleteStaff(staffId);
  }
}
