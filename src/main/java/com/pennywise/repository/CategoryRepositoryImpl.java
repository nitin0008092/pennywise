package com.pennywise.repository;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.exception.DatabaseException;
import com.pennywise.model.ExpenseCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CategoryRepository.
 */
public class CategoryRepositoryImpl implements CategoryRepository {

    private final DatabaseConnectionManager connectionManager;

    public CategoryRepositoryImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public CategoryRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ExpenseCategory save(ExpenseCategory category) {
        String sql = "INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (category.getUserId() != null) {
                ps.setLong(1, category.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, category.getName());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getColorCode());
            ps.setString(5, category.getIconName());
            ps.setBoolean(6, category.isDefault());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getLong(1));
                }
            }
            return category;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving category: " + category.getName(), e);
        }
    }

    @Override
    public Optional<ExpenseCategory> findById(Long id) {
        String sql = "SELECT * FROM expense_categories WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ExpenseCategory> findAll() {
        List<ExpenseCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM expense_categories ORDER BY is_default DESC, name ASC";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all categories", e);
        }
        return list;
    }

    @Override
    public List<ExpenseCategory> findAvailableForUser(Long userId) {
        List<ExpenseCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM expense_categories WHERE user_id IS NULL OR user_id = ? ORDER BY is_default DESC, name ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId != null ? userId : -1L);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching categories for user: " + userId, e);
        }
        return list;
    }

    @Override
    public Optional<ExpenseCategory> findByName(String name, Long userId) {
        String sql = "SELECT * FROM expense_categories WHERE LOWER(name) = ? AND (user_id IS NULL OR user_id = ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim().toLowerCase());
            ps.setLong(2, userId != null ? userId : -1L);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category by name: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean update(ExpenseCategory category) {
        String sql = "UPDATE expense_categories SET name = ?, description = ?, color_code = ?, icon_name = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getColorCode());
            ps.setString(4, category.getIconName());
            ps.setLong(5, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating category ID: " + category.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM expense_categories WHERE id = ? AND is_default = FALSE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting category ID: " + id, e);
        }
    }

    private ExpenseCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        Long userId = rs.getLong("user_id");
        if (rs.wasNull()) userId = null;

        return new ExpenseCategory(
                rs.getLong("id"),
                userId,
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("color_code"),
                rs.getString("icon_name"),
                rs.getBoolean("is_default")
        );
    }
}

