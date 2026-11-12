package com.pennywise.view.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Design system containing colors, spacing, and styling rules for PennyWise.
 */
public final class UITheme {

    // Palette Constants
    public static final String COLOR_PRIMARY = "#2563EB";       // Vibrant Blue
    public static final String COLOR_PRIMARY_DARK = "#1D4ED8";  // Darker Blue
    public static final String COLOR_NAVY = "#0F172A";          // Deep Navy Sidebar
    public static final String COLOR_NAVY_LIGHT = "#1E293B";    // Sidebar hover/active
    public static final String COLOR_BG = "#F8FAFC";            // Soft Off-White Background
    public static final String COLOR_CARD_BG = "#FFFFFF";       // Pure White Card
    public static final String COLOR_TEXT_MAIN = "#0F172A";     // Slate 900
    public static final String COLOR_TEXT_MUTED = "#64748B";    // Slate 500
    public static final String COLOR_BORDER = "#E2E8F0";        // Slate 200
    public static final String COLOR_SUCCESS = "#16A34A";       // Emerald Green
    public static final String COLOR_WARNING = "#D97706";       // Amber
    public static final String COLOR_DANGER = "#DC2626";        // Rose Red
    public static final String COLOR_TEAL = "#0D9488";          // Accent Teal

    // Fonts
    public static final String FONT_FAMILY = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif";

    private UITheme() {}

    /**
     * Applies standard card styling with subtle rounded corners and soft drop shadow.
     */
    public static void applyCardStyle(Node node) {
        node.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; " +
                      "-fx-background-radius: 12px; " +
                      "-fx-border-color: " + COLOR_BORDER + "; " +
                      "-fx-border-radius: 12px; " +
                      "-fx-border-width: 1px; " +
                      "-fx-padding: 20px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(15, 23, 42, 0.04));
        shadow.setRadius(16);
        shadow.setOffsetY(4);
        shadow.setOffsetX(0);
        node.setEffect(shadow);
    }

    /**
     * Applies primary call-to-action button styling.
     */
    public static void applyPrimaryButton(Button button) {
        String base = "-fx-background-color: " + COLOR_PRIMARY + "; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-weight: bold; " +
                      "-fx-font-size: 13px; " +
                      "-fx-background-radius: 8px; " +
                      "-fx-padding: 9px 18px; " +
                      "-fx-cursor: hand;";
        String hover = "-fx-background-color: " + COLOR_PRIMARY_DARK + ";";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(base + hover));
        button.setOnMouseExited(e -> button.setStyle(base));
    }

    /**
     * Applies secondary outline button styling.
     */
    public static void applySecondaryButton(Button button) {
        String base = "-fx-background-color: white; " +
                      "-fx-text-fill: " + COLOR_TEXT_MAIN + "; " +
                      "-fx-border-color: " + COLOR_BORDER + "; " +
                      "-fx-border-radius: 8px; " +
                      "-fx-background-radius: 8px; " +
                      "-fx-font-size: 13px; " +
                      "-fx-padding: 8px 16px; " +
                      "-fx-cursor: hand;";
        String hover = "-fx-background-color: #F1F5F9;";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(base + hover));
        button.setOnMouseExited(e -> button.setStyle(base));
    }

    /**
     * Applies danger action button styling.
     */
    public static void applyDangerButton(Button button) {
        String base = "-fx-background-color: " + COLOR_DANGER + "; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-weight: bold; " +
                      "-fx-font-size: 13px; " +
                      "-fx-background-radius: 8px; " +
                      "-fx-padding: 8px 16px; " +
                      "-fx-cursor: hand;";
        String hover = "-fx-background-color: #B91C1C;";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(base + hover));
        button.setOnMouseExited(e -> button.setStyle(base));
    }
}

