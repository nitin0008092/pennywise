package com.pennywise.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 * Provides clean geometric iconography and colored badges for JavaFX views.
 */
public final class IconHelper {

    private IconHelper() {}

    public static final String ICON_DASHBOARD = "▤";
    public static final String ICON_TRANSACTIONS = "⇄";
    public static final String ICON_INCOME = "⇣";
    public static final String ICON_EXPENSE = "⇡";
    public static final String ICON_BUDGET = "◔";
    public static final String ICON_SAVINGS = "★";
    public static final String ICON_REPORTS = "☷";
    public static final String ICON_SETTINGS = "⚙";
    public static final String ICON_BELL = "🔔";
    public static final String ICON_USER = "👤";
    public static final String ICON_SEARCH = "🔍";
    public static final String ICON_PLUS = "+";
    public static final String ICON_FILTER = "☵";
    public static final String ICON_EXPORT = "⤓";

    public static StackPane createBadge(String text, String colorHex) {
        StackPane badge = new StackPane();
        badge.setAlignment(Pos.CENTER);

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + "; " +
                       "-fx-background-color: " + colorHex + "22; " +
                       "-fx-padding: 3px 8px; -fx-background-radius: 6px;");
        badge.getChildren().add(label);
        return badge;
    }

    public static StackPane createCircleIcon(String symbol, String colorHex, double radius) {
        StackPane sp = new StackPane();
        Circle circle = new Circle(radius);
        circle.setStyle("-fx-fill: " + colorHex + "1E;");

        Label label = new Label(symbol);
        label.setStyle("-fx-font-size: " + (radius * 1.0) + "px; -fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");

        sp.getChildren().addAll(circle, label);
        return sp;
    }
}

