package com.pennywise.repository;

import com.pennywise.model.ExpenseCategory;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExpenseCategory data access.
 */
public interface CategoryRepository extends CrudRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findAvailableForUser(Long userId);

    Optional<ExpenseCategory> findByName(String name, Long userId);
}

