package com.pennywise.service;

import com.pennywise.model.User;

/**
 * Service contract for user authentication and account lifecycle.
 */
public interface AuthService {

    User register(String fullName, String email, String password, String confirmPassword);

    User login(String email, String password);

    void logout();

    boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);
}

