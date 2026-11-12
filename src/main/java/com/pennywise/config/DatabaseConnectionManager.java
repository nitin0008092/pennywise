package com.pennywise.config;

import com.pennywise.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages JDBC connection pooling with HikariCP, database initialization,
 * and transaction execution.
 */
public final class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static DatabaseConnectionManager instance;
    private HikariDataSource dataSource;
    private final DatabaseConfig config;
    private boolean isH2Fallback = false;

    private DatabaseConnectionManager(DatabaseConfig config) {
        this.config = config != null ? config : new DatabaseConfig();
        initDataSource();
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager(new DatabaseConfig());
        }
        return instance;
    }

    public static synchronized void initializeWithConfig(DatabaseConfig config) {
        if (instance != null) {
            instance.shutdown();
        }
        instance = new DatabaseConnectionManager(config);
    }

    private void initDataSource() {
        try {
            logger.info("Connecting to primary database: {}", config.getUrl());
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClassName());
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getMinIdle());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setPoolName("PennyWise-MySQL-Pool");

            dataSource = new HikariDataSource(hikariConfig);

            // Test connection
            try (Connection testConn = dataSource.getConnection()) {
                logger.info("Successfully connected to MySQL database: {}", testConn.getMetaData().getDatabaseProductName());
            }
            initializeSchema(false);
        } catch (Exception e) {
            if (config.isFallbackH2()) {
                logger.warn("Primary MySQL database connection failed: {}. Falling back to embedded MySQL-compatible H2 engine.", e.getMessage());
                initH2Fallback();
            } else {
                throw new DatabaseException("Failed to initialize database connection pool", e);
            }
        }
    }

    private void initH2Fallback() {
        try {
            isH2Fallback = true;
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:h2:./pennywise_local_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1");
            hikariConfig.setUsername("sa");
            hikariConfig.setPassword("");
            hikariConfig.setDriverClassName("org.h2.Driver");
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setConnectionTimeout(3000);
            hikariConfig.setPoolName("PennyWise-H2-Fallback-Pool");

            dataSource = new HikariDataSource(hikariConfig);
            logger.info("Embedded H2 fallback database initialized successfully.");
            initializeSchema(true);
        } catch (Exception ex) {
            throw new DatabaseException("Failed to initialize H2 fallback database", ex);
        }
    }

    /**
     * Executes the initial DDL schema.
     */
    public void initializeSchema(boolean isH2) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (isH2) {
                // Execute schema statements tailored for H2 compatibility
                executeH2Schema(stmt);
            } else {
                // Execute MySQL schema from schema.sql
                executeResourceScript(stmt, "schema.sql");
            }
            logger.info("Database schema initialized successfully.");
        } catch (SQLException e) {
            logger.error("Error executing database schema DDL", e);
            throw new DatabaseException("Could not initialize database schema", e);
        }
    }

    private void executeH2Schema(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                full_name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                password_salt VARCHAR(100) NOT NULL,
                currency VARCHAR(10) NOT NULL DEFAULT 'INR',
                monthly_budget_limit DECIMAL(15,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS expense_categories (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NULL,
                name VARCHAR(50) NOT NULL,
                description VARCHAR(255),
                color_code VARCHAR(20) DEFAULT '#2563EB',
                icon_name VARCHAR(50) DEFAULT 'tag',
                is_default BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS income (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                source VARCHAR(50) NOT NULL,
                description VARCHAR(255),
                transaction_date DATE NOT NULL,
                payment_method VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS expenses (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                category_id BIGINT NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                description VARCHAR(255),
                transaction_date DATE NOT NULL,
                payment_method VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES expense_categories(id)
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS budgets (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                category_id BIGINT NOT NULL,
                monthly_amount DECIMAL(15,2) NOT NULL,
                budget_month INT NOT NULL,
                budget_year INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES expense_categories(id)
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS savings_goals (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                goal_name VARCHAR(100) NOT NULL,
                target_amount DECIMAL(15,2) NOT NULL,
                current_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                target_date DATE NOT NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
                description VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS savings_contributions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                goal_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                contribution_date DATE NOT NULL,
                notes VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS notifications (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(150) NOT NULL,
                message TEXT NOT NULL,
                notification_type VARCHAR(30) NOT NULL DEFAULT 'INFO',
                is_read BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        // Seed default categories in H2
        seedDefaultCategoriesH2(stmt);
    }

    private void seedDefaultCategoriesH2(Statement stmt) throws SQLException {
        String[][] categories = {
            {"Food & Dining", "Groceries, restaurants, cafes, snacks", "#EF4444", "cutlery"},
            {"Transportation", "Fuel, public transit, cab fare, maintenance", "#F59E0B", "car"},
            {"Shopping", "Clothing, electronics, home goods, personal purchases", "#EC4899", "shopping-bag"},
            {"Education", "Tuition, textbooks, courses, certifications", "#6366F1", "book"},
            {"Entertainment", "Movies, streaming services, concerts, gaming", "#8B5CF6", "film"},
            {"Healthcare", "Doctor visits, medicines, lab tests, dental", "#10B981", "heart"},
            {"Housing & Rent", "Monthly rent, property taxes, home repairs", "#06B6D4", "home"},
            {"Bills & Utilities", "Electricity, water, gas, internet, mobile phone", "#3B82F6", "file-text"},
            {"Travel", "Flights, hotels, vacation expenses, holiday trips", "#14B8A6", "plane"},
            {"Personal Care", "Salon, cosmetics, gym membership, wellness", "#F43F5E", "smile"},
            {"Other", "Miscellaneous and unclassified expenses", "#64748B", "more-horizontal"}
        };

        for (String[] cat : categories) {
            String checkSql = "SELECT COUNT(*) FROM expense_categories WHERE name = '" + cat[0] + "' AND user_id IS NULL";
            try (var rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute(String.format(
                        "INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default) " +
                        "VALUES (NULL, '%s', '%s', '%s', '%s', TRUE)",
                        cat[0], cat[1], cat[2], cat[3]
                    ));
                }
            }
        }
    }

    private void executeResourceScript(Statement stmt, String resourceName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                logger.warn("Script resource {} not found on classpath, checking workspace root.", resourceName);
                java.nio.file.Path p = java.nio.file.Paths.get(resourceName);
                if (java.nio.file.Files.exists(p)) {
                    String content = java.nio.file.Files.readString(p);
                    executeSqlStatements(stmt, content);
                }
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                executeSqlStatements(stmt, sb.toString());
            }
        } catch (Exception e) {
            logger.warn("Could not execute script {}: {}", resourceName, e.getMessage());
        }
    }

    private void executeSqlStatements(Statement stmt, String script) throws SQLException {
        String[] statements = script.split(";");
        for (String sql : statements) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.toUpperCase().startsWith("CREATE DATABASE") && !trimmed.toUpperCase().startsWith("USE ")) {
                stmt.execute(trimmed);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool is not initialized");
        }
        return dataSource.getConnection();
    }

    public boolean isH2Fallback() {
        return isH2Fallback;
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shut down cleanly.");
        }
    }

    /**
     * Functional interface for transactional database operations.
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(Connection connection) throws SQLException;
    }

    /**
     * Executes the given callback inside a single atomic database transaction.
     */
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            T result = callback.doInTransaction(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            throw new DatabaseException("Transaction execution failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Failed to reset autocommit / close connection", closeEx);
                }
            }
        }
    }
}

