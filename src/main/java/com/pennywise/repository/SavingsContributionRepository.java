package com.pennywise.repository;

import com.pennywise.model.SavingsContribution;
import java.util.List;

/**
 * Repository interface for SavingsContribution data access.
 */
public interface SavingsContributionRepository extends CrudRepository<SavingsContribution, Long> {

    List<SavingsContribution> findByGoalId(Long goalId);

    List<SavingsContribution> findByUserId(Long userId);
}

