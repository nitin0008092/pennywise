package com.pennywise.service;

import com.pennywise.enums.GoalStatus;
import com.pennywise.model.SavingsContribution;
import com.pennywise.model.SavingsGoal;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for Savings Goal management.
 */
public interface SavingsGoalService {

    SavingsGoal createGoal(SavingsGoal goal);

    SavingsGoal updateGoal(SavingsGoal goal);

    boolean deleteGoal(Long goalId, Long userId);

    List<SavingsGoal> getGoalsByUserId(Long userId);

    List<SavingsGoal> getGoalsByStatus(Long userId, GoalStatus status);

    SavingsContribution addContribution(Long goalId, Long userId, BigDecimal amount, String notes);

    List<SavingsContribution> getContributions(Long goalId);

    BigDecimal getTotalSavings(Long userId);
}

