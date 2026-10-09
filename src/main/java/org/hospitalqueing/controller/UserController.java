package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.User;
import org.hospitalqueing.service.UserService;

public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  public void createUser(User user) {
    userService.createUser(user);
  }

  public User getUser(int userId) {
    return userService.getUserById(userId);
  }

  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  public void deleteUser(int userId) {
    userService.deleteUser(userId);
  }

  public void updateUser(User user) {
    userService.updateUser(user);
  }

  public User getUserByUsername(String username) {
    return userService.getUserByUsername(username);
  }

  /** Soft-deletes an account (it lands in the trash bin until restored or permanently deleted). */
  public void softDeleteUser(int userId) {
    userService.softDeleteUser(userId);
  }

  /** Restores a trashed account back to live. */
  public void restoreUser(int userId) {
    userService.restoreUser(userId);
  }

  /** Permanently removes a trashed account. */
  public void permanentlyDeleteUser(int userId) {
    userService.permanentlyDeleteUser(userId);
  }

  /** All trashed (soft-deleted) accounts. */
  public List<User> getTrashedUsers() {
    return userService.getTrashedUsers();
  }
}
