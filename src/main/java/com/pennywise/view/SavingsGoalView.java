package com.pennywise.view;

import com.pennywise.enums.GoalStatus;
import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.SavingsContribution;
import com.pennywise.model.SavingsGoal;
import com.pennywise.model.User;
import com.pennywise.service.SavingsGoalService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.MetricCard;
import com.pennywise.view.components.ProgressBarWidget;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Savings goal management screen supporting goal milestones, contribution deposits,
 * progress tracking, and completion status.
 */
public class SavingsGoalView extends VBox {

    private final SavingsGoalService savingsGoalService;
    private final Runnable onDataChanged;

    private MetricCard totalSavingsCard;
    private MetricCard activeGoalsCard;
    private MetricCard completedGoalsCard;
    private VBox goalsContainer;

    public SavingsGoalView(SavingsGoalService savingsGoalService, Runnable onDataChanged) {
        this.savingsGoalService = savingsGoalService;
        this.onDataChanged = onDataChanged;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Savings Goals");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        Label subtitle = new Label("Track and fund your long-term financial milestones");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newGoalBtn = new Button("+ Create Savings Goal");
        UITheme.applyPrimaryButton(newGoalBtn);
        newGoalBtn.setOnAction(e -> showAddEditGoalDialog(null));

        topBar.getChildren().addAll(titleBox, spacer, newGoalBtn);

        // KPI Summary Cards
        HBox kpiContainer = new HBox(16);
        totalSavingsCard = new MetricCard("Total Accumulated", "₹ 0.00", "Across all active goals", "★", UITheme.COLOR_TEAL);
        activeGoalsCard = new MetricCard("In Progress", "0 Goals", "Currently accumulating funds", "⏳", UITheme.COLOR_PRIMARY);
        completedGoalsCard = new MetricCard("Completed", "0 Goals", "Milestones achieved! 🎉", "✓", UITheme.COLOR_SUCCESS);

        HBox.setHgrow(totalSavingsCard, Priority.ALWAYS);
        HBox.setHgrow(activeGoalsCard, Priority.ALWAYS);
        HBox.setHgrow(completedGoalsCard, Priority.ALWAYS);

        kpiContainer.getChildren().addAll(totalSavingsCard, activeGoalsCard, completedGoalsCard);

        // Goals List Card
        VBox cardContainer = new VBox(14);
        UITheme.applyCardStyle(cardContainer);
        cardContainer.setPadding(new Insets(20));

        Label listHeader = new Label("Your Goals & Progress");
        listHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        goalsContainer = new VBox(16);
        cardContainer.getChildren().addAll(listHeader, goalsContainer);

        getChildren().addAll(topBar, kpiContainer, cardContainer);

        refreshData();
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        List<SavingsGoal> allGoals = savingsGoalService.getGoalsByUserId(user.getId());

        long activeCount = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS).count();
        long completedCount = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
        BigDecimal totalSaved = allGoals.stream().map(SavingsGoal::getCurrentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        totalSavingsCard.setValue(CurrencyFormatter.format(totalSaved));
        activeGoalsCard.setValue(activeCount + (activeCount == 1 ? " Goal" : " Goals"));
        completedGoalsCard.setValue(completedCount + (completedCount == 1 ? " Goal" : " Goals"));

        goalsContainer.getChildren().clear();

        if (allGoals.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            Label emptyLbl = new Label("No savings goals created yet. Click '+ Create Savings Goal' to get started!");
            emptyLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");
            emptyBox.getChildren().add(emptyLbl);
            goalsContainer.getChildren().add(emptyBox);
        } else {
            for (SavingsGoal g : allGoals) {
                goalsContainer.getChildren().add(createGoalCard(g));
            }
        }
    }

    private VBox createGoalCard(SavingsGoal g) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: " +
                      UITheme.COLOR_BORDER + "; -fx-border-radius: 10px;");

        // Top info row
        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(g.getGoalName());
        nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        String statusColor = g.getStatus() == GoalStatus.COMPLETED ? UITheme.COLOR_SUCCESS : UITheme.COLOR_PRIMARY;
        var badge = IconHelper.createBadge(g.getStatus().getDisplayName(), statusColor);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addMoneyBtn = new Button("+ Add Funds");
        UITheme.applyPrimaryButton(addMoneyBtn);
        addMoneyBtn.setStyle(addMoneyBtn.getStyle() + "-fx-padding: 4px 10px; -fx-font-size: 11px;");
        addMoneyBtn.setOnAction(e -> showAddContributionDialog(g));

        Button editBtn = new Button("Edit");
        UITheme.applySecondaryButton(editBtn);
        editBtn.setStyle(editBtn.getStyle() + "-fx-padding: 4px 8px; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> showAddEditGoalDialog(g));

        Button deleteBtn = new Button("Delete");
        UITheme.applyDangerButton(deleteBtn);
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-padding: 4px 8px; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> {
            if (AlertUtil.showConfirmation("Delete Goal", "Are you sure you want to delete goal: " + g.getGoalName() + "?")) {
                User user = SessionManager.getInstance().getCurrentUser();
                savingsGoalService.deleteGoal(g.getId(), user.getId());
                refreshData();
                onDataChanged.run();
            }
        });

        top.getChildren().addAll(nameLbl, badge, spacer, addMoneyBtn, editBtn, deleteBtn);

        // Progress bar
        ProgressBarWidget bar = new ProgressBarWidget(
                "Accumulated: " + CurrencyFormatter.format(g.getCurrentAmount()) + " of " + CurrencyFormatter.format(g.getTargetAmount()),
                g.getProgressPercentage(),
                false
        );

        // Bottom stats
        HBox bottom = new HBox(20);
        Label remLabel = new Label("Remaining: " + CurrencyFormatter.format(g.getRemainingAmount()));
        remLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Label dateLabel = new Label("Target Date: " + DateUtil.formatDate(g.getTargetDate()));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        bottom.getChildren().addAll(remLabel, dateLabel);

        card.getChildren().addAll(top, bar, bottom);
        return card;
    }

    private void showAddEditGoalDialog(SavingsGoal existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Create Savings Goal" : "Edit Savings Goal");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label(existing == null ? "New Savings Milestone" : "Edit Milestone Details");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        TextField nameField = new TextField(existing != null ? existing.getGoalName() : "");
        nameField.setPromptText("e.g. Buy a Laptop");

        TextField targetField = new TextField(existing != null ? existing.getTargetAmount().toPlainString() : "");
        targetField.setPromptText("e.g. 60000.00");

        TextField initialField = new TextField(existing != null ? existing.getCurrentAmount().toPlainString() : "0.00");
        initialField.setPromptText("e.g. 10000.00");

        DatePicker datePicker = new DatePicker(existing != null ? existing.getTargetDate() : LocalDate.now().plusMonths(6));
        datePicker.setMaxWidth(Double.MAX_VALUE);

        TextField descField = new TextField(existing != null ? existing.getDescription() : "");
        descField.setPromptText("e.g. For college coursework & programming");

        grid.add(new Label("Goal Name:*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Target Amount (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + "):*"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(new Label("Current Accumulated:"), 0, 2);
        grid.add(initialField, 1, 2);
        grid.add(new Label("Target Date:*"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descField, 1, 4);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        UITheme.applySecondaryButton(cancelBtn);
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Create Goal" : "Update");
        UITheme.applyPrimaryButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                BigDecimal target = new BigDecimal(targetField.getText().trim());
                BigDecimal current = new BigDecimal(initialField.getText().trim());
                User user = SessionManager.getInstance().getCurrentUser();

                if (existing == null) {
                    SavingsGoal goal = new SavingsGoal(user.getId(), nameField.getText().trim(), target, current,
                            datePicker.getValue(), descField.getText().trim());
                    savingsGoalService.createGoal(goal);
                } else {
                    existing.setGoalName(nameField.getText().trim());
                    existing.setTargetAmount(target);
                    existing.setCurrentAmount(current);
                    existing.setTargetDate(datePicker.getValue());
                    existing.setDescription(descField.getText().trim());
                    savingsGoalService.updateGoal(existing);
                }
                dialog.close();
                refreshData();
                onDataChanged.run();
            } catch (NumberFormatException nfe) {
                errorLbl.setText("Please enter valid numeric amounts.");
                errorLbl.setVisible(true);
            } catch (PennyWiseException ex) {
                errorLbl.setText(ex.getMessage());
                errorLbl.setVisible(true);
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(title, grid, errorLbl, btnBox);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void showAddContributionDialog(SavingsGoal goal) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Deposit Funds into Goal");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(360);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Add Funds to: " + goal.getGoalName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Label goalStatus = new Label("Current: " + CurrencyFormatter.format(goal.getCurrentAmount()) + " / " + CurrencyFormatter.format(goal.getTargetAmount()));
        goalStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 5000.00");

        TextField notesField = new TextField();
        notesField.setPromptText("e.g. Savings from monthly freelance");

        grid.add(new Label("Deposit Amount:*"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(notesField, 1, 1);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        UITheme.applySecondaryButton(cancelBtn);
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Add Deposit");
        UITheme.applyPrimaryButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                User user = SessionManager.getInstance().getCurrentUser();
                savingsGoalService.addContribution(goal.getId(), user.getId(), amt, notesField.getText().trim());
                dialog.close();
                refreshData();
                onDataChanged.run();
            } catch (NumberFormatException nfe) {
                errorLbl.setText("Please enter a valid numeric amount.");
                errorLbl.setVisible(true);
            } catch (PennyWiseException ex) {
                errorLbl.setText(ex.getMessage());
                errorLbl.setVisible(true);
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(title, goalStatus, grid, errorLbl, btnBox);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}

