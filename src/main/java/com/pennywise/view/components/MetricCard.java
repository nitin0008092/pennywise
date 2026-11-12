package com.pennywise.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Modern KPI metric card for financial dashboards.
 */
public class MetricCard extends VBox {

    private final Label titleLabel;
    private final Label valueLabel;
    private final Label subLabel;
    private final StackPane iconContainer;

    public MetricCard(String title, String initialValue, String subtitle, String iconSymbol, String accentColor) {
        setSpacing(12);
        setPadding(new Insets(18));
        setPrefWidth(240);
        UITheme.applyCardStyle(this);

        // Header: Title and Icon
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; " +
                            "-fx-font-size: 13px; -fx-font-weight: 500;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Circular Icon Badge
        iconContainer = new StackPane();
        Circle circle = new Circle(18);
        circle.setStyle("-fx-fill: " + accentColor + "22;"); // 15% opacity hex
        Label iconLabel = new Label(iconSymbol);
        iconLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + accentColor + "; -fx-font-weight: bold;");
        iconContainer.getChildren().addAll(circle, iconLabel);

        header.getChildren().addAll(titleLabel, spacer, iconContainer);

        // Value Label
        valueLabel = new Label(initialValue);
        valueLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + "; " +
                            "-fx-font-size: 22px; -fx-font-weight: bold;");

        // Subtitle / Trend Badge
        subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");

        getChildren().addAll(header, valueLabel, subLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setValueColor(String colorHex) {
        valueLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 22px; -fx-font-weight: bold;");
    }

    public void setSubtitle(String subtitle) {
        subLabel.setText(subtitle);
    }
}

