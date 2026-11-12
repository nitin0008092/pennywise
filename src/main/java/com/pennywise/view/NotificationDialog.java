package com.pennywise.view;

import com.pennywise.model.Notification;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.UITheme;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog displaying financial notifications and smart rule-based insights.
 */
public class NotificationDialog extends Stage {

    public NotificationDialog(List<Notification> notifications, Runnable onMarkAllRead) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Financial Insights & Alerts");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(480);
        root.setPrefHeight(450);
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label("Smart Insights & Alerts");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markReadBtn = new Button("Mark all read");
        UITheme.applySecondaryButton(markReadBtn);
        markReadBtn.setOnAction(e -> {
            onMarkAllRead.run();
            close();
        });

        header.getChildren().addAll(titleLbl, spacer, markReadBtn);

        // Notification List
        VBox listContainer = new VBox(10);
        listContainer.setPadding(new Insets(4));

        if (notifications == null || notifications.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            Label emptyLbl = new Label("No financial alerts at this moment.");
            emptyLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");
            emptyBox.getChildren().add(emptyLbl);
            listContainer.getChildren().add(emptyBox);
        } else {
            for (Notification n : notifications) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(12));
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: " +
                              UITheme.COLOR_BORDER + "; -fx-border-radius: 8px;");

                HBox cardTop = new HBox(8);
                cardTop.setAlignment(Pos.CENTER_LEFT);

                Label notifTitle = new Label(n.getTitle());
                notifTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                var badge = IconHelper.createBadge(n.getType().name(), n.getType().getColorHex());
                cardTop.getChildren().addAll(notifTitle, sp, badge);

                Label msgLbl = new Label(n.getMessage());
                msgLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
                msgLbl.setWrapText(true);

                Label timeLbl = new Label(DateUtil.formatDateTime(n.getCreatedAt()));
                timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

                card.getChildren().addAll(cardTop, msgLbl, timeLbl);
                listContainer.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button closeBtn = new Button("Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        UITheme.applyPrimaryButton(closeBtn);
        closeBtn.setOnAction(e -> close());

        root.getChildren().addAll(header, scrollPane, closeBtn);
        setScene(new Scene(root));
    }
}

