package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.User;

public class UserService {
  private final UserDAO userDAO;

  public UserService(UserDAO userDAO) {
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

  public void updateUser(User user) {
    userDAO.update(user);
  }

  public User getUserByUsername(String username) {
    return userDAO.findByUsername(username);
  }

  /** Soft-deletes an account: it moves to the trash bin, recoverable via restore. */
  public void softDeleteUser(int userId) {
    userDAO.softDelete(userId);
  }

  /** Restores a trashed account back to live. */
  public void restoreUser(int userId) {
    userDAO.restore(userId);
  }

  /** Permanently removes a trashed account. */
  public void permanentlyDeleteUser(int userId) {
    userDAO.permanentlyDelete(userId);
  }

  /** All trashed (soft-deleted) accounts. */
  public List<User> getTrashedUsers() {
    return userDAO.findTrashed();
  }
}
