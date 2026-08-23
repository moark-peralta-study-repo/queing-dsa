package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.RoleDAO;
import org.hospitalqueing.model.Role;

public class RoleService {
  private final RoleDAO roleDAO;

  RoleService(RoleDAO roleDAO) {
    this.roleDAO = roleDAO;
  }

  public void createRole(Role role) {
    roleDAO.save(role);
  }

  public Role getRoleById(int roleId) {
    return roleDAO.findById(roleId);
  }

  public Role getRoleByName(String roleName) {
    return roleDAO.findByName(roleName);
  }

  public List<Role> getAllRoles() {
    return roleDAO.findAll();
  }

  public void updateRole(Role role) {
    roleDAO.update(role);
  }

  public void deleteRole(int roleId) {
    roleDAO.delete(roleId);
  }
}
