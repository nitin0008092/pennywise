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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Modern user registration screen.
 */
public class RegisterView extends StackPane {

    private final AuthService authService;
    private final Consumer<User> onRegisterSuccess;
    private final Runnable onNavigateToLogin;

    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;

    public RegisterView(AuthService authService, Consumer<User> onRegisterSuccess, Runnable onNavigateToLogin) {
        this.authService = authService;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateToLogin = onNavigateToLogin;

        setStyle("-fx-background-color: " + UITheme.COLOR_BG + ";");
        buildUI();
    }

    private void buildUI() {
        VBox card = new VBox(18);
        card.setMaxWidth(440);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER);
        UITheme.applyCardStyle(card);

        // Header
        Label title = new Label("Create an Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Take control of your personal finances with PennyWise");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        VBox header = new VBox(6, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // Form Fields
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label("Full Name");
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        nameField = new TextField();
        nameField.setPromptText("e.g. Rahul Sharma");
        styleInput(nameField);

        Label emailLbl = new Label("Email Address");
        emailLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        emailField = new TextField();
        emailField.setPromptText("e.g. rahul@example.com");
        styleInput(emailField);

        Label passLbl = new Label("Password (min. 6 characters with letters and numbers)");
        passLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter a secure password");
        styleInput(passwordField);

        Label confirmLbl = new Label("Confirm Password");
        confirmLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        styleInput(confirmPasswordField);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px; -fx-font-weight: 500;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        form.getChildren().addAll(nameLbl, nameField, emailLbl, emailField, passLbl, passwordField, confirmLbl, confirmPasswordField, errorLabel);

        // Buttons
        Button registerBtn = new Button("Register & Get Started");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        UITheme.applyPrimaryButton(registerBtn);
        registerBtn.setOnAction(e -> handleRegister());

        // Back to Login link
        HBox bottomBox = new HBox(4);
        bottomBox.setAlignment(Pos.CENTER);
        Label haveAcc = new Label("Already have an account?");
        haveAcc.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");
        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setStyle("-fx-text-fill: " + UITheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: transparent;");
        loginLink.setOnAction(e -> onNavigateToLogin.run());
        bottomBox.getChildren().addAll(haveAcc, loginLink);

        card.getChildren().addAll(header, form, registerBtn, bottomBox);
        getChildren().add(card);
    }

    private void handleRegister() {
        errorLabel.setVisible(false);
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        try {
            User user = authService.register(name, email, pass, confirm);
            onRegisterSuccess.accept(user);
        } catch (PennyWiseException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
        } catch (Exception ex) {
            errorLabel.setText("Registration error: " + ex.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private void styleInput(TextField field) {
        field.setStyle("-fx-background-color: white; " +
                       "-fx-border-color: " + UITheme.COLOR_BORDER + "; " +
                       "-fx-border-radius: 8px; " +
                       "-fx-background-radius: 8px; " +
                       "-fx-padding: 9px 12px; " +
                       "-fx-font-size: 13px;");
        field.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) {
                field.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + UITheme.COLOR_PRIMARY + "; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 9px 12px; " +
                               "-fx-font-size: 13px;");
            } else {
                field.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + UITheme.COLOR_BORDER + "; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 9px 12px; " +
                               "-fx-font-size: 13px;");
            }
        });
    }
}

