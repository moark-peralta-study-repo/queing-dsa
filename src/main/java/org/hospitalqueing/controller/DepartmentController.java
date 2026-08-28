package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Department;
import org.hospitalqueing.service.DepartmentService;

public class DepartmentController {
  private final DepartmentService departmentService;

  public DepartmentController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  public void createDepartment(Department department) {
    departmentService.createDepartment(department);
  }

  public Department getDepartment(int departmentId) {
    return departmentService.getDepartmentById(departmentId);
  }

  public List<Department> getAllDepartments() {
    return departmentService.getAllDepartments();
  }

  public void updateDepartment(Department department) {
    departmentService.updateDepartment(department);
  }

  public void deleteDepartment(int departmentId) {
    departmentService.deleteDepartment(departmentId);
  }
}
