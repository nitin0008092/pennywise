package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.enums.PaymentMethod;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.Expense;
import com.pennywise.model.ExpenseCategory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC implementation of ExpenseRepository.
 */
public class ExpenseRepositoryImpl implements ExpenseRepository {

    private final DatabaseConnectionManager connectionManager;

    public ExpenseRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public ExpenseRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Expense save(Expense expense) {
        String sql = "INSERT INTO expenses (user_id, category_id, amount, description, transaction_date, payment_method, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, expense.getUserId());
            ps.setLong(2, expense.getCategoryId());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, Date.valueOf(expense.getTransactionDate()));
            ps.setString(6, expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : PaymentMethod.CASH.name());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expense.setId(keys.getLong(1));
                }
            }
            return expense;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving expense record", e);
        }
    }

    @Override
    public Optional<Expense> findById(Long id) {
        String sql = "SELECT e.*, c.name AS cat_name, c.description AS cat_desc, c.color_code, c.icon_name, c.is_default, c.user_id AS cat_user_id " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "WHERE e.id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding expense by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Expense> findAll() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.*, c.name AS cat_name, c.description AS cat_desc, c.color_code, c.icon_name, c.is_default, c.user_id AS cat_user_id " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "ORDER BY e.transaction_date DESC, e.id DESC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all expenses", e);
        }
        return list;
    }

    @Override
    public List<Expense> findByUserId(Long userId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.*, c.name AS cat_name, c.description AS cat_desc, c.color_code, c.icon_name, c.is_default, c.user_id AS cat_user_id " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? " +
                     "ORDER BY e.transaction_date DESC, e.id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding expenses for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Expense> findByUserIdAndDateRange(Long userId, LocalDate from, LocalDate to) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.*, c.name AS cat_name, c.description AS cat_desc, c.color_code, c.icon_name, c.is_default, c.user_id AS cat_user_id " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? AND e.transaction_date BETWEEN ? AND ? " +
                     "ORDER BY e.transaction_date DESC, e.id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying expenses by date range for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Expense> findByUserIdAndCategory(Long userId, Long categoryId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.*, c.name AS cat_name, c.description AS cat_desc, c.color_code, c.icon_name, c.is_default, c.user_id AS cat_user_id " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? AND e.category_id = ? " +
                     "ORDER BY e.transaction_date DESC, e.id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying expenses by category for user: " + userId, e);
        }
        return list;
    }

    @Override
    public BigDecimal getTotalExpenseByUserId(Long userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total expense for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalExpenseByUserIdAndMonth(Long userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
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
            throw new DatabaseException("Error calculating monthly expense for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalSpentForCategoryInMonth(Long userId, Long categoryId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND category_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating category monthly spent for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> getSpendingByCategoryForMonth(Long userId, int month, int year) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT c.name, COALESCE(SUM(e.amount), 0) AS total " +
                     "FROM expenses e " +
                     "JOIN expense_categories c ON e.category_id = c.id " +
                     "WHERE e.user_id = ? AND MONTH(e.transaction_date) = ? AND YEAR(e.transaction_date) = ? " +
                     "GROUP BY c.name " +
                     "ORDER BY total DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating spending by category for user: " + userId, e);
        }
        return map;
    }

    @Override
    public Map<LocalDate, BigDecimal> getDailySpendingTrends(Long userId, LocalDate from, LocalDate to) {
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT transaction_date, SUM(amount) AS daily_total " +
                     "FROM expenses " +
                     "WHERE user_id = ? AND transaction_date BETWEEN ? AND ? " +
                     "GROUP BY transaction_date " +
                     "ORDER BY transaction_date ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("transaction_date").toLocalDate(), rs.getBigDecimal("daily_total"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching daily spending trends for user: " + userId, e);
        }
        return map;
    }

    @Override
    public boolean update(Expense expense) {
        String sql = "UPDATE expenses SET category_id = ?, amount = ?, description = ?, transaction_date = ?, payment_method = ? " +
                     "WHERE id = ? AND user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, expense.getCategoryId());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setString(3, expense.getDescription());
            ps.setDate(4, Date.valueOf(expense.getTransactionDate()));
            ps.setString(5, expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : PaymentMethod.CASH.name());
            ps.setLong(6, expense.getId());
            ps.setLong(7, expense.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating expense ID: " + expense.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting expense ID: " + id, e);
        }
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");

        Long catUserId = rs.getLong("cat_user_id");
        if (rs.wasNull()) catUserId = null;

        ExpenseCategory category = new ExpenseCategory(
                rs.getLong("category_id"),
                catUserId,
                rs.getString("cat_name"),
                rs.getString("cat_desc"),
                rs.getString("color_code"),
                rs.getString("icon_name"),
                rs.getBoolean("is_default")
        );

        return new Expense(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("category_id"),
                category,
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getDate("transaction_date").toLocalDate(),
                PaymentMethod.fromString(rs.getString("payment_method")),
                createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now()
        );
    }
}

