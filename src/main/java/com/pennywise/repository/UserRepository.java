package com.pennywise.repository;

import com.pennywise.enums.CurrencyUnit;
import com.pennywise.model.User;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository interface for User entity data access.
 */
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean updatePassword(Long userId, String passwordHash, String passwordSalt);

    boolean updatePreferences(Long userId, CurrencyUnit currency, BigDecimal monthlyBudgetLimit);

    boolean updateProfile(Long userId, String fullName, String email);
}

