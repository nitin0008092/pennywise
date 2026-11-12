package com.pennywise.validation;

import com.pennywise.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates user registration and profile update inputs.
 */
public final class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private UserValidator() {}

    public static ValidationResult<User> validateRegistration(String fullName, String email, String password, String confirmPassword) {
        List<String> errors = new ArrayList<>();

        if (fullName == null || fullName.trim().length() < 2) {
            errors.add("Full name must be at least 2 characters long.");
        } else if (fullName.trim().length() > 100) {
            errors.add("Full name cannot exceed 100 characters.");
        }

        if (email == null || email.trim().isEmpty()) {
            errors.add("Email address is required.");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("Please provide a valid email address (e.g., user@example.com).");
        }

        if (password == null || password.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        } else if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            errors.add("Password must contain both letters and at least one digit.");
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.add("Passwords do not match.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors, null);
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        return ValidationResult.success(user);
    }

    public static ValidationResult<String> validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("Email address cannot be empty.", email);
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return ValidationResult.failure("Invalid email format.", email);
        }
        return ValidationResult.success(email.trim().toLowerCase());
    }

    public static ValidationResult<String> validatePassword(String password) {
        if (password == null || password.length() < 6) {
            return ValidationResult.failure("Password must be at least 6 characters long.", password);
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            return ValidationResult.failure("Password must contain both letters and digits.", password);
        }
        return ValidationResult.success(password);
    }
}

