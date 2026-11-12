-- ============================================================================
-- PennyWise - Personal Finance & Expense Management System
-- Database Schema for MySQL 8.x
-- ============================================================================

CREATE DATABASE IF NOT EXISTS pennywise_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pennywise_db;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(100) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    monthly_budget_limit DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. EXPENSE CATEGORIES TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    color_code VARCHAR(20) DEFAULT '#2563EB',
    icon_name VARCHAR(50) DEFAULT 'tag',
    is_default BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. INCOME TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    source VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_income_user (user_id),
    INDEX idx_income_user_date (user_id, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. EXPENSES TABLE
-- ----------------------------------------------------------------------------
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
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    INDEX idx_expense_user (user_id),
    INDEX idx_expense_user_date (user_id, transaction_date),
    INDEX idx_expense_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. BUDGETS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    monthly_amount DECIMAL(15,2) NOT NULL,
    budget_month INT NOT NULL CHECK (budget_month BETWEEN 1 AND 12),
    budget_year INT NOT NULL CHECK (budget_year >= 2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_cat_month_year (user_id, category_id, budget_month, budget_year),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    INDEX idx_budget_user_period (user_id, budget_year, budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. SAVINGS GOALS TABLE
-- ----------------------------------------------------------------------------
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_savings_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 7. SAVINGS CONTRIBUTIONS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS savings_contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    contribution_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_contribution_goal (goal_id),
    INDEX idx_contribution_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 8. NOTIFICATIONS & INSIGHTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- SEED DEFAULT EXPENSE CATEGORIES
-- ----------------------------------------------------------------------------
INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Food & Dining', 'Groceries, restaurants, cafes, snacks', '#EF4444', 'cutlery', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Food & Dining' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Transportation', 'Fuel, public transit, cab fare, maintenance', '#F59E0B', 'car', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Transportation' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Shopping', 'Clothing, electronics, home goods, personal purchases', '#EC4899', 'shopping-bag', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Shopping' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Education', 'Tuition, textbooks, courses, certifications', '#6366F1', 'book', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Education' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Entertainment', 'Movies, streaming services, concerts, gaming', '#8B5CF6', 'film', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Entertainment' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Healthcare', 'Doctor visits, medicines, lab tests, dental', '#10B981', 'heart', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Healthcare' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Housing & Rent', 'Monthly rent, property taxes, home repairs', '#06B6D4', 'home', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Housing & Rent' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Bills & Utilities', 'Electricity, water, gas, internet, mobile phone', '#3B82F6', 'file-text', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Bills & Utilities' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Travel', 'Flights, hotels, vacation expenses, holiday trips', '#14B8A6', 'plane', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Travel' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Personal Care', 'Salon, cosmetics, gym membership, wellness', '#F43F5E', 'smile', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Personal Care' AND user_id IS NULL);

INSERT INTO expense_categories (user_id, name, description, color_code, icon_name, is_default)
SELECT NULL, 'Other', 'Miscellaneous and unclassified expenses', '#64748B', 'more-horizontal', TRUE
WHERE NOT EXISTS (SELECT 1 FROM expense_categories WHERE name = 'Other' AND user_id IS NULL);

