package com.pennywise.repository;

import com.pennywise.enums.GoalStatus;
import com.pennywise.model.SavingsGoal;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for SavingsGoal data access.
 */
public interface SavingsGoalRepository extends CrudRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

    List<SavingsGoal> findByUserIdAndStatus(Long userId, GoalStatus status);

    BigDecimal getTotalSavingsByUserId(Long userId);

    boolean updateCurrentAmount(Long goalId, BigDecimal newAmount);

    boolean updateStatus(Long goalId, GoalStatus status);
}

