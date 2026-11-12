package com.pennywise.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Custom modern progress bar widget with dynamic color transitions and percentage readout.
 */
public class ProgressBarWidget extends VBox {

    private final Region track;
    private final Region fill;
    private final Label percentLabel;
    private final Label infoLabel;

    public ProgressBarWidget(String initialLabel, double initialPercentage, boolean isBudgetMetric) {
        setSpacing(6);

        // Header info and percentage
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        infoLabel = new Label(initialLabel);
        infoLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + "; -fx-font-size: 13px; -fx-font-weight: 500;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        percentLabel = new Label(String.format("%.1f%%", initialPercentage));
        percentLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        top.getChildren().addAll(infoLabel, spacer, percentLabel);

        // Progress Track and Fill
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setPrefHeight(8);
        barContainer.setMaxHeight(8);

        track = new Region();
        track.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 4px;");
        track.setPrefHeight(8);
        track.prefWidthProperty().bind(barContainer.widthProperty());

        fill = new Region();
        fill.setPrefHeight(8);

        barContainer.getChildren().addAll(track, fill);

        getChildren().addAll(top, barContainer);

        setProgress(initialPercentage, isBudgetMetric);
    }

    public void setProgress(double percentage, boolean isBudgetMetric) {
        double clamped = Math.clamp(percentage, 0.0, 100.0);
        percentLabel.setText(String.format("%.1f%%", percentage));

        String color;
        if (isBudgetMetric) {
            if (percentage >= 90.0) {
                color = UITheme.COLOR_DANGER; // Red
            } else if (percentage >= 70.0) {
                color = UITheme.COLOR_WARNING; // Orange
            } else {
                color = UITheme.COLOR_SUCCESS; // Green
            }
        } else {
            // Savings goal metric
            if (percentage >= 100.0) {
                color = UITheme.COLOR_SUCCESS;
            } else {
                color = UITheme.COLOR_PRIMARY;
            }
        }

        percentLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Bind fill width proportionally to container width
        fill.prefWidthProperty().bind(track.widthProperty().multiply(clamped / 100.0));
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4px;");
    }

    public void setInfoText(String text) {
        infoLabel.setText(text);
    }
}

