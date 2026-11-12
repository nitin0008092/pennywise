package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.SavingsContribution;
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
 * JDBC implementation of SavingsContributionRepository.
 */
public class SavingsContributionRepositoryImpl implements SavingsContributionRepository {

    private final DatabaseConnectionManager connectionManager;

    public SavingsContributionRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public SavingsContributionRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public SavingsContribution save(SavingsContribution contribution) {
        String sql = "INSERT INTO savings_contributions (goal_id, user_id, amount, contribution_date, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, contribution.getGoalId());
            ps.setLong(2, contribution.getUserId());
            ps.setBigDecimal(3, contribution.getAmount());
            ps.setDate(4, Date.valueOf(contribution.getContributionDate()));
            ps.setString(5, contribution.getNotes());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    contribution.setId(keys.getLong(1));
                }
            }
            return contribution;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving savings contribution", e);
        }
    }

    @Override
    public Optional<SavingsContribution> findById(Long id) {
        String sql = "SELECT * FROM savings_contributions WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToContribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding contribution by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SavingsContribution> findAll() {
        List<SavingsContribution> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_contributions ORDER BY contribution_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToContribution(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all savings contributions", e);
        }
        return list;
    }

    @Override
    public List<SavingsContribution> findByGoalId(Long goalId) {
        List<SavingsContribution> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_contributions WHERE goal_id = ? ORDER BY contribution_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToContribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching contributions for goal: " + goalId, e);
        }
        return list;
    }

    @Override
    public List<SavingsContribution> findByUserId(Long userId) {
        List<SavingsContribution> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_contributions WHERE user_id = ? ORDER BY contribution_date DESC, id DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToContribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching contributions for user: " + userId, e);
        }
        return list;
    }

    @Override
    public boolean update(SavingsContribution contribution) {
        String sql = "UPDATE savings_contributions SET amount = ?, contribution_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, contribution.getAmount());
            ps.setDate(2, Date.valueOf(contribution.getContributionDate()));
            ps.setString(3, contribution.getNotes());
            ps.setLong(4, contribution.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating contribution ID: " + contribution.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM savings_contributions WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting contribution ID: " + id, e);
        }
    }

    private SavingsContribution mapResultSetToContribution(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new SavingsContribution(
                rs.getLong("id"),
                rs.getLong("goal_id"),
                rs.getLong("user_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("contribution_date").toLocalDate(),
                rs.getString("notes"),
                createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now()
        );
    }
}

