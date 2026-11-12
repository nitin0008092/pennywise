package com.pennywise.view;

import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.Budget;
import com.pennywise.model.ExpenseCategory;
import com.pennywise.model.User;
import com.pennywise.service.BudgetService;
import com.pennywise.service.ExpenseService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.MetricCard;
import com.pennywise.view.components.ProgressBarWidget;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Budget management screen with category limits, real-time utilization progress bars,
 * and colored threshold indicators (Green, Orange, Red).
 */
public class BudgetView extends VBox {

    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final Runnable onDataChanged;

    private ComboBox<String> monthPicker;
    private ComboBox<Integer> yearPicker;
    private MetricCard totalBudgetCard;
    private MetricCard totalSpentCard;
    private MetricCard remainingBudgetCard;
    private MetricCard utilizationCard;
    private VBox budgetListContainer;

    public BudgetView(BudgetService budgetService, ExpenseService expenseService, Runnable onDataChanged) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.onDataChanged = onDataChanged;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar with Month-Year Selector & Add Button
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Monthly Budget Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        Label subtitle = new Label("Set category spending allowances and monitor thresholds");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Month Picker
        monthPicker = new ComboBox<>();
        for (int m = 1; m <= 12; m++) {
            monthPicker.getItems().add(DateUtil.getMonthName(m));
        }
        monthPicker.setValue(DateUtil.getMonthName(LocalDate.now().getMonthValue()));
        monthPicker.setOnAction(e -> refreshData());

        // Year Picker
        yearPicker = new ComboBox<>(FXCollections.observableArrayList(2024, 2025, 2026, 2027, 2028));
        yearPicker.setValue(LocalDate.now().getYear());
        yearPicker.setOnAction(e -> refreshData());

        Button addBudgetBtn = new Button("+ Set Category Budget");
        UITheme.applyPrimaryButton(addBudgetBtn);
        addBudgetBtn.setOnAction(e -> showAddEditDialog(null));

        topBar.getChildren().addAll(titleBox, spacer, monthPicker, yearPicker, addBudgetBtn);

        // KPI Summary Cards
        HBox kpiContainer = new HBox(16);
        totalBudgetCard = new MetricCard("Total Budget", "₹ 0.00", "Total allocated limits", "◔", UITheme.COLOR_PRIMARY);
        totalSpentCard = new MetricCard("Total Spent", "₹ 0.00", "Total category expenditures", "⇡", UITheme.COLOR_DANGER);
        remainingBudgetCard = new MetricCard("Remaining", "₹ 0.00", "Available before limit", "⚖", UITheme.COLOR_SUCCESS);
        utilizationCard = new MetricCard("Overall Utilization", "0.0%", "Safe limit: < 70%", "%", UITheme.COLOR_WARNING);

        HBox.setHgrow(totalBudgetCard, Priority.ALWAYS);
        HBox.setHgrow(totalSpentCard, Priority.ALWAYS);
        HBox.setHgrow(remainingBudgetCard, Priority.ALWAYS);
        HBox.setHgrow(utilizationCard, Priority.ALWAYS);

        kpiContainer.getChildren().addAll(totalBudgetCard, totalSpentCard, remainingBudgetCard, utilizationCard);

        // Budget Items List Container
        VBox cardContainer = new VBox(14);
        UITheme.applyCardStyle(cardContainer);
        cardContainer.setPadding(new Insets(20));

        Label listHeader = new Label("Category Budgets & Threshold Indicators");
        listHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        HBox legendBox = new HBox(16);
        legendBox.setAlignment(Pos.CENTER_LEFT);
        legendBox.getChildren().addAll(
                IconHelper.createBadge("Safe (< 70%)", UITheme.COLOR_SUCCESS),
                IconHelper.createBadge("Warning (70% - 90%)", UITheme.COLOR_WARNING),
                IconHelper.createBadge("Danger / Exceeded (> 90%)", UITheme.COLOR_DANGER)
        );

        budgetListContainer = new VBox(16);
        cardContainer.getChildren().addAll(listHeader, legendBox, budgetListContainer);

        getChildren().addAll(topBar, kpiContainer, cardContainer);

        refreshData();
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int month = getSelectedMonth();
        int year = getSelectedYear();

        List<Budget> budgets = budgetService.getBudgetsForPeriod(user.getId(), month, year);

        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Budget b : budgets) {
            totalBudget = totalBudget.add(b.getMonthlyAmount());
            totalSpent = totalSpent.add(b.getSpentAmount());
        }

        BigDecimal remaining = totalBudget.subtract(totalSpent);
        double overallUtil = 0.0;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            overallUtil = totalSpent.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 1, RoundingMode.HALF_UP).doubleValue();
        }

        totalBudgetCard.setValue(CurrencyFormatter.format(totalBudget));
        totalSpentCard.setValue(CurrencyFormatter.format(totalSpent));
        remainingBudgetCard.setValue(CurrencyFormatter.format(remaining));
        utilizationCard.setValue(String.format("%.1f%%", overallUtil));

        if (overallUtil >= 90.0) {
            utilizationCard.setValueColor(UITheme.COLOR_DANGER);
        } else if (overallUtil >= 70.0) {
            utilizationCard.setValueColor(UITheme.COLOR_WARNING);
        } else {
            utilizationCard.setValueColor(UITheme.COLOR_SUCCESS);
        }

        // Render Budget Items
        budgetListContainer.getChildren().clear();

        if (budgets.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            Label emptyLbl = new Label("No category budgets set for " + monthPicker.getValue() + " " + year + ".");
            emptyLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");
            emptyBox.getChildren().add(emptyLbl);
            budgetListContainer.getChildren().add(emptyBox);
        } else {
            for (Budget b : budgets) {
                budgetListContainer.getChildren().add(createBudgetItemCard(b));
            }
        }
    }

    private VBox createBudgetItemCard(Budget b) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: " +
                      UITheme.COLOR_BORDER + "; -fx-border-radius: 10px;");

        // Top info row
        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label catName = new Label(b.getCategoryName());
        catName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        String statusText;
        String statusColor;
        double util = b.getUtilizationPercentage();
        if (util >= 100.0) {
            statusText = "EXCEEDED";
            statusColor = UITheme.COLOR_DANGER;
        } else if (util >= 90.0) {
            statusText = "DANGER";
            statusColor = UITheme.COLOR_DANGER;
        } else if (util >= 70.0) {
            statusText = "WARNING";
            statusColor = UITheme.COLOR_WARNING;
        } else {
            statusText = "ON TRACK";
            statusColor = UITheme.COLOR_SUCCESS;
        }

        var badge = IconHelper.createBadge(statusText + " (" + String.format("%.1f%%", util) + ")", statusColor);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        UITheme.applySecondaryButton(editBtn);
        editBtn.setStyle(editBtn.getStyle() + "-fx-padding: 3px 8px; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> showAddEditDialog(b));

        Button deleteBtn = new Button("Delete");
        UITheme.applyDangerButton(deleteBtn);
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-padding: 3px 8px; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> {
            if (AlertUtil.showConfirmation("Delete Budget", "Remove budget allocation for " + b.getCategoryName() + "?")) {
                User user = SessionManager.getInstance().getCurrentUser();
                budgetService.deleteBudget(b.getId(), user.getId());
                refreshData();
                onDataChanged.run();
            }
        });

        top.getChildren().addAll(catName, badge, spacer, editBtn, deleteBtn);

        // Progress Bar Widget
        ProgressBarWidget bar = new ProgressBarWidget(
                "Spent: " + CurrencyFormatter.format(b.getSpentAmount()) + " of " + CurrencyFormatter.format(b.getMonthlyAmount()),
                b.getUtilizationPercentage(),
                true
        );

        // Bottom stats
        HBox bottom = new HBox(20);
        Label remLabel = new Label("Remaining: " + CurrencyFormatter.format(b.getRemainingAmount()));
        remLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " +
                          (b.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0 ? UITheme.COLOR_DANGER : UITheme.COLOR_SUCCESS) + ";");

        Label limitLabel = new Label("Budget Limit: " + CurrencyFormatter.format(b.getMonthlyAmount()));
        limitLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");

        bottom.getChildren().addAll(remLabel, limitLabel);

        card.getChildren().addAll(top, bar, bottom);
        return card;
    }

    private void showAddEditDialog(Budget existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Set Budget" : "Edit Budget");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label(existing == null ? "Set Category Monthly Budget" : "Update Category Budget");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        User user = SessionManager.getInstance().getCurrentUser();
        List<ExpenseCategory> categories = expenseService.getAvailableCategories(user.getId());

        ComboBox<ExpenseCategory> catBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        if (existing != null) {
            for (ExpenseCategory c : categories) {
                if (c.getId().equals(existing.getCategoryId())) {
                    catBox.setValue(c);
                    break;
                }
            }
            catBox.setDisable(true); // Don't change category on edit
        } else if (!categories.isEmpty()) {
            catBox.setValue(categories.getFirst());
        }
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField(existing != null ? existing.getMonthlyAmount().toPlainString() : "");
        amountField.setPromptText("e.g. 5000.00");

        grid.add(new Label("Expense Category:*"), 0, 0);
        grid.add(catBox, 1, 0);
        grid.add(new Label("Monthly Limit (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + "):*"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Budget Period:"), 0, 2);
        grid.add(new Label(monthPicker.getValue() + " " + yearPicker.getValue()), 1, 2);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        UITheme.applySecondaryButton(cancelBtn);
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Save Budget" : "Update");
        UITheme.applyPrimaryButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                ExpenseCategory selectedCat = catBox.getValue();
                if (selectedCat == null) {
                    throw new PennyWiseException("Please select a category.");
                }

                int month = getSelectedMonth();
                int year = getSelectedYear();

                if (existing == null) {
                    Budget newBudget = new Budget(user.getId(), selectedCat.getId(), amt, month, year);
                    budgetService.setBudget(newBudget);
                } else {
                    existing.setMonthlyAmount(amt);
                    budgetService.setBudget(existing);
                }
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
        root.getChildren().addAll(title, grid, errorLbl, btnBox);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private int getSelectedMonth() {
        String mName = monthPicker.getValue();
        for (int i = 1; i <= 12; i++) {
            if (DateUtil.getMonthName(i).equalsIgnoreCase(mName)) {
                return i;
            }
        }
        return LocalDate.now().getMonthValue();
    }

    private int getSelectedYear() {
        return yearPicker.getValue() != null ? yearPicker.getValue() : LocalDate.now().getYear();
    }
}

