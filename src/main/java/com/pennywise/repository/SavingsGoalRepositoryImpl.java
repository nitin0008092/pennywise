package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.enums.GoalStatus;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.SavingsGoal;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
 * JDBC implementation of SavingsGoalRepository.
 */
public class SavingsGoalRepositoryImpl implements SavingsGoalRepository {

    private final DatabaseConnectionManager connectionManager;

    public SavingsGoalRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public SavingsGoalRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public SavingsGoal save(SavingsGoal goal) {
        String sql = "INSERT INTO savings_goals (user_id, goal_name, target_amount, current_amount, target_date, status, description, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, goal.getUserId());
            ps.setString(2, goal.getGoalName());
            ps.setBigDecimal(3, goal.getTargetAmount());
            ps.setBigDecimal(4, goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            ps.setDate(5, Date.valueOf(goal.getTargetDate()));
            ps.setString(6, goal.getStatus() != null ? goal.getStatus().name() : GoalStatus.IN_PROGRESS.name());
            ps.setString(7, goal.getDescription());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setId(keys.getLong(1));
                }
            }
            return goal;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving savings goal", e);
        }
    }

    @Override
    public Optional<SavingsGoal> findById(Long id) {
        String sql = "SELECT * FROM savings_goals WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding savings goal by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SavingsGoal> findAll() {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goals ORDER BY target_date ASC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all savings goals", e);
        }
        return list;
    }

    @Override
    public List<SavingsGoal> findByUserId(Long userId) {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goals WHERE user_id = ? ORDER BY target_date ASC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching savings goals for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<SavingsGoal> findByUserIdAndStatus(Long userId, GoalStatus status) {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goals WHERE user_id = ? AND status = ? ORDER BY target_date ASC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, status != null ? status.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching savings goals by status for user: " + userId, e);
        }
        return list;
    }

    @Override
    public BigDecimal getTotalSavingsByUserId(Long userId) {
        String sql = "SELECT COALESCE(SUM(current_amount), 0) FROM savings_goals WHERE user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total savings for user: " + userId, e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean updateCurrentAmount(Long goalId, BigDecimal newAmount) {
        String sql = "UPDATE savings_goals SET current_amount = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newAmount);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, goalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating savings goal current amount: " + goalId, e);
        }
    }

    @Override
    public boolean updateStatus(Long goalId, GoalStatus status) {
        String sql = "UPDATE savings_goals SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, goalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating savings goal status: " + goalId, e);
        }
    }

    @Override
    public boolean update(SavingsGoal goal) {
        String sql = "UPDATE savings_goals SET goal_name = ?, target_amount = ?, current_amount = ?, target_date = ?, status = ?, description = ?, updated_at = ? " +
                     "WHERE id = ? AND user_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getGoalName());
            ps.setBigDecimal(2, goal.getTargetAmount());
            ps.setBigDecimal(3, goal.getCurrentAmount());
            ps.setDate(4, Date.valueOf(goal.getTargetDate()));
            ps.setString(5, goal.getStatus() != null ? goal.getStatus().name() : GoalStatus.IN_PROGRESS.name());
            ps.setString(6, goal.getDescription());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(8, goal.getId());
            ps.setLong(9, goal.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating savings goal ID: " + goal.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM savings_goals WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting savings goal ID: " + id, e);
        }
    }

    private SavingsGoal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new SavingsGoal(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("goal_name"),
                rs.getBigDecimal("target_amount"),
                rs.getBigDecimal("current_amount"),
                rs.getDate("target_date").toLocalDate(),
                GoalStatus.fromString(rs.getString("status")),
                rs.getString("description"),
                createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now(),
                updatedTs != null ? updatedTs.toLocalDateTime() : LocalDateTime.now()
        );
    }
}

