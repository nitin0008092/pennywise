package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.enums.IncomeSource;
import com.pennywise.enums.PaymentMethod;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.Income;
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
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of IncomeRepository.
 */
public class IncomeRepositoryImpl implements IncomeRepository {

    private final DatabaseConnectionManager connectionManager;

    public IncomeRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public IncomeRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Income save(Income income) {
        String sql = "INSERT INTO income (user_id, amount, source, description, transaction_date, payment_method, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, income.getUserId());
            ps.setBigDecimal(2, income.getAmount());
            ps.setString(3, income.getSource() != null ? income.getSource().name() : IncomeSource.OTHER.name());
            ps.setString(4, income.getDescription());
            ps.setDate(5, Date.valueOf(income.getTransactionDate()));
            ps.setString(6, income.getPaymentMethod() != null ? income.getPaymentMethod().name() : PaymentMethod.CASH.name());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    income.setId(keys.getLong(1));
                }
            }
            return income;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving income record", e);
        }
    }

    @Override
    public Optional<Income> findById(Long id) {
        String sql = "SELECT * FROM income WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding income by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Income> findAll() {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income ORDER BY transaction_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToIncome(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all income records", e);
        }
        return list;
    }

    @Override
    public List<Income> findByUserId(Long userId) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? ORDER BY transaction_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding income for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Income> findByUserIdAndDateRange(Long userId, LocalDate from, LocalDate to) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying income by date range for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Income> findByUserIdAndSource(Long userId, IncomeSource source) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? AND source = ? ORDER BY transaction_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, source != null ? source.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIncome(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding income by source for user: " + userId, e);
        }
        return list;
    }

    @Override
    public BigDecimal getTotalIncomeByUserId(Long userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total income for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalIncomeByUserIdAndMonth(Long userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM income WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
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
            throw new DatabaseException("Error calculating monthly income for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean update(Income income) {
        String sql = "UPDATE income SET amount = ?, source = ?, description = ?, transaction_date = ?, payment_method = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, income.getAmount());
            ps.setString(2, income.getSource() != null ? income.getSource().name() : IncomeSource.OTHER.name());
            ps.setString(3, income.getDescription());
            ps.setDate(4, Date.valueOf(income.getTransactionDate()));
            ps.setString(5, income.getPaymentMethod() != null ? income.getPaymentMethod().name() : PaymentMethod.CASH.name());
            ps.setLong(6, income.getId());
            ps.setLong(7, income.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating income ID: " + income.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM income WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting income ID: " + id, e);
        }
    }

    private Income mapResultSetToIncome(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Income(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getBigDecimal("amount"),
                IncomeSource.fromString(rs.getString("source")),
                rs.getString("description"),
                rs.getDate("transaction_date").toLocalDate(),
                PaymentMethod.fromString(rs.getString("payment_method")),
                createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now()
        );
    }
}

