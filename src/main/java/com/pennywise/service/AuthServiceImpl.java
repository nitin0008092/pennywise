package com.pennywise.service;

import com.pennywise.exception.AuthenticationException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.User;
import com.pennywise.repository.UserRepository;
import com.pennywise.repository.UserRepositoryImpl;
import com.pennywise.util.PasswordHasher;
import com.pennywise.validation.UserValidator;
import com.pennywise.validation.ValidationResult;
import java.util.Optional;

/**
 * Service implementation for authentication operations.
 */
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthServiceImpl() {
        this.userRepository = new UserRepositoryImpl();
        this.sessionManager = SessionManager.getInstance();
    }

    public AuthServiceImpl(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public User register(String fullName, String email, String password, String confirmPassword) {
        ValidationResult<User> validation = UserValidator.validateRegistration(fullName, email, password, confirmPassword);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with this email address already exists.");
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        User newUser = new User(fullName.trim(), normalizedEmail, hash, salt);
        User savedUser = userRepository.save(newUser);
        sessionManager.setCurrentUser(savedUser);
        return savedUser;
    }

    @Override
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Email and password are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmail(normalizedEmail);
        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = optionalUser.get();
        boolean valid = PasswordHasher.verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt());
        if (!valid) {
            throw new AuthenticationException("Invalid email or password.");
        }

        sessionManager.setCurrentUser(user);
        return user;
    }

    @Override
    public void logout() {
        sessionManager.clearSession();
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (userId == null) {
            throw new AuthenticationException("User is not authenticated.");
        }

        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            throw new AuthenticationException("User account not found.");
        }

        User user = opt.get();
        if (!PasswordHasher.verifyPassword(currentPassword, user.getPasswordHash(), user.getPasswordSalt())) {
            throw new AuthenticationException("Incorrect current password.");
        }

        ValidationResult<String> pwdValidation = UserValidator.validatePassword(newPassword);
        if (!pwdValidation.isValid()) {
            throw new ValidationException(pwdValidation.getFirstError());
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new ValidationException("New passwords do not match.");
        }

        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
        boolean updated = userRepository.updatePassword(userId, newHash, newSalt);
        if (updated) {
            user.setPasswordHash(newHash);
            user.setPasswordSalt(newSalt);
        }
        return updated;
    }
}

