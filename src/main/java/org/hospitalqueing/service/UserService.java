package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.User;

public class UserService {
  private final UserDAO userDAO;

  UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public void createUser(User user) {
    userDAO.save(user);
  }

  public User getUserById(int userId) {
    return userDAO.findById(userId);
  }

  public List<User> getAllUsers() {
    return userDAO.findAll();
  }

  public void deleteUser(int userId) {
    userDAO.delete(userId);
  }
}
