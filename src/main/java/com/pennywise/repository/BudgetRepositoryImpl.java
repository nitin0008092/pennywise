package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.Budget;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of BudgetRepository.
 */
public class BudgetRepositoryImpl implements BudgetRepository {

    private final DatabaseConnectionManager connectionManager;

    public BudgetRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public BudgetRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Budget save(Budget budget) {
        // Upsert behavior using standard SQL
        Optional<Budget> existing = findByUserCategoryAndPeriod(
                budget.getUserId(), budget.getCategoryId(), budget.getBudgetMonth(), budget.getBudgetYear());
        if (existing.isPresent()) {
            budget.setId(existing.get().getId());
            update(budget);
            return budget;
        }

        String sql = "INSERT INTO budgets (user_id, category_id, monthly_amount, budget_month, budget_year, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, budget.getUserId());
            ps.setLong(2, budget.getCategoryId());
            ps.setBigDecimal(3, budget.getMonthlyAmount());
            ps.setInt(4, budget.getBudgetMonth());
            ps.setInt(5, budget.getBudgetYear());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    budget.setId(keys.getLong(1));
                }
            }
            return budget;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving budget record", e);
        }
    }

    @Override
    public Optional<Budget> findById(Long id) {
        String sql = "SELECT b.*, c.name AS cat_name, c.color_code AS cat_color " +
                     "FROM budgets b " +
                     "JOIN expense_categories c ON b.category_id = c.id " +
                     "WHERE b.id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding budget by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Budget> findAll() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT b.*, c.name AS cat_name, c.color_code AS cat_color " +
                     "FROM budgets b " +
                     "JOIN expense_categories c ON b.category_id = c.id " +
                     "ORDER BY b.budget_year DESC, b.budget_month DESC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToBudget(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all budgets", e);
        }
        return list;
    }

    @Override
    public List<Budget> findByUserIdAndPeriod(Long userId, int month, int year) {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT b.*, c.name AS cat_name, c.color_code AS cat_color, " +
                     "  (SELECT COALESCE(SUM(e.amount), 0) FROM expenses e " +
                     "   WHERE e.user_id = b.user_id AND e.category_id = b.category_id " +
                     "   AND MONTH(e.transaction_date) = b.budget_month AND YEAR(e.transaction_date) = b.budget_year) AS spent_amount " +
                     "FROM budgets b " +
                     "JOIN expense_categories c ON b.category_id = c.id " +
                     "WHERE b.user_id = ? AND b.budget_month = ? AND b.budget_year = ? " +
                     "ORDER BY c.name ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Budget b = mapResultSetToBudget(rs);
                    b.setSpentAmount(rs.getBigDecimal("spent_amount"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching budgets for user: " + userId + " period: " + month + "/" + year, e);
        }
        return list;
    }

    @Override
    public Optional<Budget> findByUserCategoryAndPeriod(Long userId, Long categoryId, int month, int year) {
        String sql = "SELECT b.*, c.name AS cat_name, c.color_code AS cat_color " +
                     "FROM budgets b " +
                     "JOIN expense_categories c ON b.category_id = c.id " +
                     "WHERE b.user_id = ? AND b.category_id = ? AND b.budget_month = ? AND b.budget_year = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying budget by category and period", e);
        }
        return Optional.empty();
    }

    @Override
    public BigDecimal getTotalBudgetByUserIdAndPeriod(Long userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(monthly_amount), 0) FROM budgets WHERE user_id = ? AND budget_month = ? AND budget_year = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total budget for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET monthly_amount = ?, updated_at = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, budget.getMonthlyAmount());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, budget.getId());
            ps.setLong(4, budget.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating budget ID: " + budget.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting budget ID: " + id, e);
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new Budget(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("category_id"),
                rs.getString("cat_name"),
                rs.getString("cat_color"),
                rs.getBigDecimal("monthly_amount"),
                rs.getInt("budget_month"),
                rs.getInt("budget_year"),
                createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now(),
                updatedTs != null ? updatedTs.toLocalDateTime() : LocalDateTime.now()
        );
    }
}

