package com.pennywise.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import java.util.Optional;

/**
 * Utility for displaying cleanly styled JavaFX dialogues and confirmation prompts.
 */
public final class AlertUtil {

    private AlertUtil() {}

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialogPane(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialogPane(alert.getDialogPane());
        alert.showAndWait();
    }

    private static void styleDialogPane(DialogPane pane) {
        pane.setStyle("-fx-font-family: 'Segoe UI', -apple-system, sans-serif; " +
                      "-fx-font-size: 13px; " +
                      "-fx-background-color: #FFFFFF;");
    }
}

