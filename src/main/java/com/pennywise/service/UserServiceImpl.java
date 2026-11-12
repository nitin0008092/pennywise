package com.pennywise.service;

import com.pennywise.enums.CurrencyUnit;
import com.pennywise.exception.ResourceNotFoundException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.User;
import com.pennywise.repository.UserRepository;
import com.pennywise.repository.UserRepositoryImpl;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.validation.UserValidator;
import com.pennywise.validation.ValidationResult;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service implementation for user profile updates.
 */
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public UserServiceImpl() {
        this.userRepository = new UserRepositoryImpl();
        this.sessionManager = SessionManager.getInstance();
    }

    public UserServiceImpl(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public User updateProfile(Long userId, String fullName, String email) {
        if (fullName == null || fullName.trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters.");
        }
        ValidationResult<String> emailVal = UserValidator.validateEmail(email);
        if (!emailVal.isValid()) {
            throw new ValidationException(emailVal.getFirstError());
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new ValidationException("Email address is already in use by another account.");
        }

        userRepository.updateProfile(userId, fullName.trim(), normalizedEmail);
        User updated = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        sessionManager.setCurrentUser(updated);
        return updated;
    }

    @Override
    public User updatePreferences(Long userId, CurrencyUnit currency, BigDecimal monthlyBudgetLimit) {
        if (monthlyBudgetLimit != null && monthlyBudgetLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Budget limit cannot be negative.");
        }
        userRepository.updatePreferences(userId, currency, monthlyBudgetLimit);
        User updated = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        sessionManager.setCurrentUser(updated);
        CurrencyFormatter.setDefaultCurrency(currency);
        return updated;
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}

