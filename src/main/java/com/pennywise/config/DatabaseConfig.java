package com.pennywise.config;

/**
 * Encapsulates database connection properties and pool parameters.
 * Guarantees that credentials are not hardcoded in source code.
 */
public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
    private final int maxPoolSize;
    private final int minIdle;
    private final long connectionTimeout;
    private final boolean fallbackH2;

    public DatabaseConfig() {
        this.url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/pennywise_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        this.username = AppConfig.get("db.username", "root");
        this.password = AppConfig.get("db.password", "");
        this.driverClassName = AppConfig.get("db.driver", "com.mysql.cj.jdbc.Driver");
        this.maxPoolSize = AppConfig.getInt("db.pool.maximumPoolSize", 10);
        this.minIdle = AppConfig.getInt("db.pool.minimumIdle", 2);
        this.connectionTimeout = AppConfig.getInt("db.pool.connectionTimeout", 5000);
        this.fallbackH2 = AppConfig.getBoolean("db.fallback.h2", true);
    }

    public DatabaseConfig(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.maxPoolSize = 5;
        this.minIdle = 1;
        this.connectionTimeout = 5000;
        this.fallbackH2 = false;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public boolean isFallbackH2() {
        return fallbackH2;
    }
}

