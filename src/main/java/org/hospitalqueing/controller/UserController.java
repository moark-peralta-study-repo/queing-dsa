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
}
