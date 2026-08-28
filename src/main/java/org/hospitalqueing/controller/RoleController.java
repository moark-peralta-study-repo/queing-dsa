package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Role;
import org.hospitalqueing.service.RoleService;

public class RoleController {
  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  public void createRole(Role role) {
    roleService.createRole(role);
  }

  public Role getRole(int roleId) {
    return roleService.getRoleById(roleId);
  }

  public Role getRoleByName(String roleName) {
    return roleService.getRoleByName(roleName);
  }

  public List<Role> getAllRoles() {
    return roleService.getAllRoles();
  }

  public void updateRole(Role role) {
    roleService.updateRole(role);
  }

  public void deleteRole(int roleId) {
    roleService.deleteRole(roleId);
  }
}
