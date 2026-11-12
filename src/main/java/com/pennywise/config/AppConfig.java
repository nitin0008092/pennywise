package com.pennywise.config;

import com.pennywise.enums.CurrencyUnit;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global application configuration reader.
 * Reads settings from application.properties with fallback to environment variables.
 */
public final class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            logger.warn("Could not load application.properties, falling back to defaults.", e);
        }
    }

    private AppConfig() {}

    public static String get(String key, String defaultValue) {
        // First check environment variables (capitalized with underscores)
        String envKey = key.replace('.', '_').toUpperCase();
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal;
        }

        // Second check system properties
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }

        // Third check properties file
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val.trim());
    }

    public static CurrencyUnit getDefaultCurrency() {
        String code = get("app.default.currency", "INR");
        return CurrencyUnit.fromCode(code);
    }
}

