package org.hospitalqueing.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import org.hospitalqueing.dao.UserDAO;
import org.hospitalqueing.model.User;

public class AuthenticationService {
  private final UserDAO userDAO;

  public AuthenticationService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public User login(String username, String password) {
    if (username == null || password == null) {
      return null;
    }

    for (User user : userDAO.findAll()) {
      if (user.getUsername().equals(username)) {
        if (!user.isActive()) {
          return null;
        }

        String stored = user.getPasswordHash();
        if (stored == null || stored.isEmpty()) {
          return null;
        }

        String[] parts = stored.split(":");
        if (parts.length != 2) {
          return null;
        }

        String salt = parts[0];
        String expected = parts[1];

        String actual = hash(password, salt);
        if (constantTimeEquals(expected, actual)) {
          return user;
        }
        return null;
      }
    }

    return null;
  }

  public User getUserById(int userId) {
    return userDAO.findById(userId);
  }

  public String hashPassword(String plainText) {
    if (plainText == null) {
      return null;
    }

    SecureRandom random = new SecureRandom();
    byte[] saltBytes = new byte[16];
    random.nextBytes(saltBytes);

    String salt = HexFormat.of().formatHex(saltBytes);
    return salt + ":" + hash(plainText, salt);
  }

  public boolean verifyPassword(String plainText, String storedHash) {
    if (plainText == null || storedHash == null || storedHash.isEmpty()) {
      return false;
    }

    String[] parts = storedHash.split(":");
    if (parts.length != 2) {
      return false;
    }

    return constantTimeEquals(parts[1], hash(plainText, parts[0]));
  }

  private static String hash(String plainText, String salt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(salt.getBytes(StandardCharsets.UTF_8));
      byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) {
      return false;
    }

    byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
    byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

    int result = aBytes.length ^ bBytes.length;
    int min = Math.min(aBytes.length, bBytes.length);

    for (int i = 0; i < min; i++) {
      result |= aBytes[i] ^ bBytes[i];
    }

    return result == 0;
  }
}
