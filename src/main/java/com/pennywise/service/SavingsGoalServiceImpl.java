package com.pennywise.service;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.enums.GoalStatus;
import com.pennywise.exception.ResourceNotFoundException;
import com.pennywise.exception.ValidationException;
import com.pennywise.model.SavingsContribution;
import com.pennywise.model.SavingsGoal;
import com.pennywise.repository.SavingsContributionRepository;
import com.pennywise.repository.SavingsContributionRepositoryImpl;
import com.pennywise.repository.SavingsGoalRepository;
import com.pennywise.repository.SavingsGoalRepositoryImpl;
import com.pennywise.validation.SavingsGoalValidator;
import com.pennywise.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Savings Goal operations with atomic transaction support.
 */
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final SavingsContributionRepository contributionRepository;
    private final DatabaseConnectionManager connectionManager;

    public SavingsGoalServiceImpl() {
        this.goalRepository = new SavingsGoalRepositoryImpl();
        this.contributionRepository = new SavingsContributionRepositoryImpl();
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public SavingsGoalServiceImpl(SavingsGoalRepository goalRepository,
                                  SavingsContributionRepository contributionRepository,
                                  DatabaseConnectionManager connectionManager) {
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.connectionManager = connectionManager;
    }

    @Override
    public SavingsGoal createGoal(SavingsGoal goal) {
        ValidationResult<SavingsGoal> validation = SavingsGoalValidator.validate(goal);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        return goalRepository.save(goal);
    }

    @Override
    public SavingsGoal updateGoal(SavingsGoal goal) {
        ValidationResult<SavingsGoal> validation = SavingsGoalValidator.validate(goal);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getCombinedErrors());
        }
        SavingsGoal existing = goalRepository.findById(goal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found: " + goal.getId()));

        if (!existing.getUserId().equals(goal.getUserId())) {
            throw new ValidationException("Unauthorized modification of savings goal.");
        }

        // Auto check completed status
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        boolean updated = goalRepository.update(goal);
        if (!updated) {
            throw new ValidationException("Failed to update savings goal.");
        }
        return goal;
    }

    @Override
    public boolean deleteGoal(Long goalId, Long userId) {
        SavingsGoal existing = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found: " + goalId));

        if (!existing.getUserId().equals(userId)) {
            throw new ValidationException("Unauthorized deletion of savings goal.");
        }

        return goalRepository.deleteById(goalId);
    }

    @Override
    public List<SavingsGoal> getGoalsByUserId(Long userId) {
        return goalRepository.findByUserId(userId);
    }

    @Override
    public List<SavingsGoal> getGoalsByStatus(Long userId, GoalStatus status) {
        return goalRepository.findByUserIdAndStatus(userId, status);
    }

    /**
     * Atomically adds a contribution and updates the goal's current accumulated amount.
     * Demonstrates JDBC Transactions and ACID compliance.
     */
    @Override
    public SavingsContribution addContribution(Long goalId, Long userId, BigDecimal amount, String notes) {
        ValidationResult<BigDecimal> amountVal = SavingsGoalValidator.validateContribution(amount);
        if (!amountVal.isValid()) {
            throw new ValidationException(amountVal.getFirstError());
        }

        return connectionManager.executeInTransaction(conn -> {
            SavingsGoal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found: " + goalId));

            if (!goal.getUserId().equals(userId)) {
                throw new ValidationException("Unauthorized access to savings goal.");
            }

            // 1. Save Contribution record
            SavingsContribution contribution = new SavingsContribution(goalId, userId, amount, LocalDate.now(), notes);
            SavingsContribution saved = contributionRepository.save(contribution);

            // 2. Increment Goal accumulated amount
            BigDecimal newTotal = goal.getCurrentAmount().add(amount);
            goal.setCurrentAmount(newTotal);

            // Check if goal reached
            if (newTotal.compareTo(goal.getTargetAmount()) >= 0) {
                goal.setStatus(GoalStatus.COMPLETED);
            }

            goalRepository.update(goal);
            return saved;
        });
    }

    @Override
    public List<SavingsContribution> getContributions(Long goalId) {
        return contributionRepository.findByGoalId(goalId);
    }

    @Override
    public BigDecimal getTotalSavings(Long userId) {
        return goalRepository.getTotalSavingsByUserId(userId);
    }
}

