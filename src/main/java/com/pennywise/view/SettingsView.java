package com.pennywise.view;

import com.pennywise.config.DatabaseConnectionManager;
import com.pennywise.enums.CurrencyUnit;
import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.User;
import com.pennywise.service.AuthService;
import com.pennywise.service.SessionManager;
import com.pennywise.service.UserService;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * User profile, application preferences, and security settings screen.
 */
public class SettingsView extends VBox {

    private final UserService userService;
    private final AuthService authService;
    private final Runnable onPreferencesUpdated;

    private TextField nameField;
    private TextField emailField;
    private ComboBox<CurrencyUnit> currencyCombo;
    private TextField monthlyLimitField;
    private Label memberSinceLabel;

    public SettingsView(UserService userService, AuthService authService, Runnable onPreferencesUpdated) {
        this.userService = userService;
        this.authService = authService;
        this.onPreferencesUpdated = onPreferencesUpdated;

        setSpacing(24);
        buildUI();
    }

    private void buildUI() {
        // Page Title
        VBox titleBox = new VBox(4);
        Label title = new Label("User Profile & Application Preferences");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        Label subtitle = new Label("Manage personal credentials, default currency, and security options");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(title, subtitle);

        HBox columns = new HBox(24);

        // Left Column: Profile & Currency Preferences
        VBox leftCard = new VBox(16);
        leftCard.setPadding(new Insets(24));
        leftCard.setPrefWidth(460);
        UITheme.applyCardStyle(leftCard);

        Label profileHeader = new Label("Personal Information & Currency");
        profileHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(12);
        profileGrid.setVgap(14);

        nameField = new TextField();
        emailField = new TextField();

        currencyCombo = new ComboBox<>(FXCollections.observableArrayList(CurrencyUnit.values()));
        currencyCombo.setMaxWidth(Double.MAX_VALUE);

        monthlyLimitField = new TextField();
        monthlyLimitField.setPromptText("e.g. 50000.00");

        memberSinceLabel = new Label("Member since: -");
        memberSinceLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");

        profileGrid.add(new Label("Full Name:*"), 0, 0);
        profileGrid.add(nameField, 1, 0);
        profileGrid.add(new Label("Email Address:*"), 0, 1);
        profileGrid.add(emailField, 1, 1);
        profileGrid.add(new Label("Default Currency:*"), 0, 2);
        profileGrid.add(currencyCombo, 1, 2);
        profileGrid.add(new Label("Overall Monthly Limit:"), 0, 3);
        profileGrid.add(monthlyLimitField, 1, 3);

        Button saveProfileBtn = new Button("Save Profile & Preferences");
        UITheme.applyPrimaryButton(saveProfileBtn);
        saveProfileBtn.setOnAction(e -> handleSaveProfile());

        leftCard.getChildren().addAll(profileHeader, profileGrid, memberSinceLabel, saveProfileBtn);

        // Right Column: Change Password Card & System Info
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(420);

        // Change Password Card
        VBox pwdCard = new VBox(16);
        pwdCard.setPadding(new Insets(24));
        UITheme.applyCardStyle(pwdCard);

        Label pwdHeader = new Label("Security & Password");
        pwdHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane pwdGrid = new GridPane();
        pwdGrid.setHgap(12);
        pwdGrid.setVgap(14);

        PasswordField currentPwdField = new PasswordField();
        currentPwdField.setPromptText("Current password");

        PasswordField newPwdField = new PasswordField();
        newPwdField.setPromptText("New password (min 6 chars)");

        PasswordField confirmPwdField = new PasswordField();
        confirmPwdField.setPromptText("Confirm new password");

        pwdGrid.add(new Label("Current Password:*"), 0, 0);
        pwdGrid.add(currentPwdField, 1, 0);
        pwdGrid.add(new Label("New Password:*"), 0, 1);
        pwdGrid.add(newPwdField, 1, 1);
        pwdGrid.add(new Label("Confirm Password:*"), 0, 2);
        pwdGrid.add(confirmPwdField, 1, 2);

        Button changePwdBtn = new Button("Update Password");
        UITheme.applySecondaryButton(changePwdBtn);
        changePwdBtn.setOnAction(e -> {
            try {
                User user = SessionManager.getInstance().getCurrentUser();
                authService.changePassword(user.getId(), currentPwdField.getText(), newPwdField.getText(), confirmPwdField.getText());
                currentPwdField.clear();
                newPwdField.clear();
                confirmPwdField.clear();
                AlertUtil.showInfo("Password Updated", "Your password has been changed successfully.");
            } catch (PennyWiseException ex) {
                AlertUtil.showError("Security Error", ex.getMessage());
            }
        });

        pwdCard.getChildren().addAll(pwdHeader, pwdGrid, changePwdBtn);

        // System Diagnostic Card
        VBox sysCard = new VBox(10);
        sysCard.setPadding(new Insets(18));
        UITheme.applyCardStyle(sysCard);

        Label sysHeader = new Label("System & Architecture Information");
        sysHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        boolean isH2 = DatabaseConnectionManager.getInstance().isH2Fallback();
        String dbInfo = isH2 ? "Embedded H2 (MySQL Mode)" : "MySQL 8.x Server (JDBC Pool)";
        String javaVersion = System.getProperty("java.version");

        Label dbLbl = new Label("Database Engine: " + dbInfo);
        dbLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        Label jvmLbl = new Label("Java Runtime: JDK " + javaVersion);
        jvmLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        Label oopLbl = new Label("Architecture: 4-Layer OOP MVC (Pure JavaFX)");
        oopLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        sysCard.getChildren().addAll(sysHeader, dbLbl, jvmLbl, oopLbl);

        rightColumn.getChildren().addAll(pwdCard, sysCard);

        columns.getChildren().addAll(leftCard, rightColumn);
        getChildren().addAll(titleBox, columns);

        refreshData();
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        nameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        currencyCombo.setValue(user.getCurrency() != null ? user.getCurrency() : CurrencyUnit.INR);
        monthlyLimitField.setText(user.getMonthlyBudgetLimit() != null ? user.getMonthlyBudgetLimit().toPlainString() : "0.00");
        memberSinceLabel.setText("Account created: " + DateUtil.formatDateTime(user.getCreatedAt()));
    }

    private void handleSaveProfile() {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            CurrencyUnit cu = currencyCombo.getValue();
            BigDecimal limit = new BigDecimal(monthlyLimitField.getText().trim());

            userService.updateProfile(user.getId(), name, email);
            userService.updatePreferences(user.getId(), cu, limit);

            AlertUtil.showInfo("Preferences Saved", "Your profile and currency preferences have been updated.");
            refreshData();
            onPreferencesUpdated.run();
        } catch (NumberFormatException nfe) {
            AlertUtil.showError("Validation Error", "Monthly limit must be a valid number.");
        } catch (PennyWiseException ex) {
            AlertUtil.showError("Update Error", ex.getMessage());
        }
    }
}

