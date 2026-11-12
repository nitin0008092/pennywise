package com.pennywise.view;

import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.User;
import com.pennywise.service.AuthService;
import com.pennywise.view.components.UITheme;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Modern login screen for user authentication.
 */
public class LoginView extends StackPane {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onNavigateToRegister;

    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;

    public LoginView(AuthService authService, Consumer<User> onLoginSuccess, Runnable onNavigateToRegister) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToRegister = onNavigateToRegister;

        setStyle("-fx-background-color: " + UITheme.COLOR_BG + ";");
        buildUI();
    }

    private void buildUI() {
        VBox card = new VBox(20);
        card.setMaxWidth(420);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER);
        UITheme.applyCardStyle(card);

        // Logo & Title Header
        Label logoIcon = new Label("₹");
        logoIcon.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-background-color: " + UITheme.COLOR_PRIMARY + "; " +
                          "-fx-background-radius: 12px; -fx-padding: 8px 18px;");

        Label appName = new Label("PennyWise");
        appName.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Personal Finance & Expense Management");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        VBox headerBox = new VBox(8, logoIcon, appName, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Form Fields
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);

        Label emailLbl = new Label("Email Address");
        emailLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        emailField = new TextField();
        emailField.setPromptText("e.g. alex@example.com");
        styleInput(emailField);

        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        styleInput(passwordField);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px; -fx-font-weight: 500;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        form.getChildren().addAll(emailLbl, emailField, passLbl, passwordField, errorLabel);

        // Sign In Button
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        UITheme.applyPrimaryButton(loginBtn);
        loginBtn.setOnAction(e -> handleLogin());

        // Enter key shortcut
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());

        // Quick Demo Fill Button (for testing convenience)
        Button demoBtn = new Button("Prefill Demo Credentials");
        demoBtn.setMaxWidth(Double.MAX_VALUE);
        UITheme.applySecondaryButton(demoBtn);
        demoBtn.setOnAction(e -> {
            emailField.setText("demo@pennywise.com");
            passwordField.setText("Demo@123");
            errorLabel.setVisible(false);
        });

        // Register Link
        HBox bottomBox = new HBox(4);
        bottomBox.setAlignment(Pos.CENTER);
        Label noAccLbl = new Label("Don't have an account?");
        noAccLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");
        Hyperlink regLink = new Hyperlink("Create Account");
        regLink.setStyle("-fx-text-fill: " + UITheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: transparent;");
        regLink.setOnAction(e -> onNavigateToRegister.run());
        bottomBox.getChildren().addAll(noAccLbl, regLink);

        card.getChildren().addAll(headerBox, form, loginBtn, demoBtn, bottomBox);
        getChildren().add(card);
    }

    private void handleLogin() {
        errorLabel.setVisible(false);
        String email = emailField.getText();
        String pass = passwordField.getText();

        try {
            User user = authService.login(email, pass);
            onLoginSuccess.accept(user);
        } catch (PennyWiseException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
        } catch (Exception ex) {
            errorLabel.setText("Login error: " + ex.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private void styleInput(TextField field) {
        field.setStyle("-fx-background-color: white; " +
                       "-fx-border-color: " + UITheme.COLOR_BORDER + "; " +
                       "-fx-border-radius: 8px; " +
                       "-fx-background-radius: 8px; " +
                       "-fx-padding: 10px 14px; " +
                       "-fx-font-size: 13px;");
        field.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) {
                field.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + UITheme.COLOR_PRIMARY + "; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 10px 14px; " +
                               "-fx-font-size: 13px;");
            } else {
                field.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + UITheme.COLOR_BORDER + "; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 10px 14px; " +
                               "-fx-font-size: 13px;");
            }
        });
    }
}

