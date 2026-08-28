package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.model.Department;

public class DepartmentService {
  private final DepartmentDAO departmentDAO;

  public DepartmentService(DepartmentDAO departmentDAO) {
    this.departmentDAO = departmentDAO;
  }

  public void createDepartment(Department department) {
    departmentDAO.save(department);
  }

  public Department getDepartmentById(int departmentId) {
    return departmentDAO.findById(departmentId);
  }

  public List<Department> getAllDepartments() {
    return departmentDAO.findAll();
  }

  public void updateDepartment(Department department) {
    departmentDAO.update(department);
  }

  public void deleteDepartment(int departmentId) {
    departmentDAO.delete(departmentId);
  }
}
