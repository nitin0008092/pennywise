package com.pennywise.service;

import com.pennywise.enums.CurrencyUnit;
import com.pennywise.model.User;
import java.math.BigDecimal;

/**
 * Service contract for user preferences and profile settings.
 */
public interface UserService {

    User updateProfile(Long userId, String fullName, String email);

    User updatePreferences(Long userId, CurrencyUnit currency, BigDecimal monthlyBudgetLimit);

    User getUserById(Long userId);
}

