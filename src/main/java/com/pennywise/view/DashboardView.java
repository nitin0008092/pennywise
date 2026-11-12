package com.pennywise.view;

import com.pennywise.enums.TransactionType;
import com.pennywise.model.Budget;
import com.pennywise.model.FinancialSummary;
import com.pennywise.model.Transaction;
import com.pennywise.model.User;
import com.pennywise.service.BudgetService;
import com.pennywise.service.ExpenseService;
import com.pennywise.service.IncomeService;
import com.pennywise.service.SessionManager;
import com.pennywise.service.TransactionService;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.MetricCard;
import com.pennywise.view.components.ProgressBarWidget;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Main dashboard featuring financial KPI cards, interactive JavaFX charts,
 * recent transactions table, and budget utilization trackers.
 */
public class DashboardView extends VBox {

    private final TransactionService transactionService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    // KPI Cards
    private MetricCard incomeCard;
    private MetricCard expenseCard;
    private MetricCard balanceCard;
    private MetricCard savingsCard;
    private MetricCard budgetCard;

    // Charts
    private PieChart categoryPieChart;
    private BarChart<String, Number> incomeExpenseBarChart;
    private LineChart<String, Number> spendingTrendLineChart;

    // Tables & Progress
    private TableView<Transaction> recentTransactionsTable;
    private VBox budgetProgressContainer;

    public DashboardView(TransactionService transactionService, IncomeService incomeService,
                         ExpenseService expenseService, BudgetService budgetService) {
        this.transactionService = transactionService;
        this.incomeService = incomeService;
        this.expenseService = expenseService;
        this.budgetService = budgetService;

        setSpacing(24);
        buildUI();
    }

    private void buildUI() {
        // Welcome Header
        User user = SessionManager.getInstance().getCurrentUser();
        String name = user != null ? user.getFullName() : "User";

        VBox welcomeBox = new VBox(4);
        Label greeting = new Label("Welcome back, " + name + " 👋");
        greeting.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        Label summaryText = new Label("Here is your real-time financial snapshot for " + DateUtil.getMonthName(LocalDate.now().getMonthValue()) + " " + LocalDate.now().getYear());
        summaryText.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        welcomeBox.getChildren().addAll(greeting, summaryText);

        // 1. KPI Metric Cards Grid
        HBox kpiContainer = new HBox(16);
        kpiContainer.setAlignment(Pos.CENTER_LEFT);

        incomeCard = new MetricCard("Total Income", "₹ 0.00", "Current month earnings", "⇣", UITheme.COLOR_SUCCESS);
        expenseCard = new MetricCard("Total Expenses", "₹ 0.00", "Current month spending", "⇡", UITheme.COLOR_DANGER);
        balanceCard = new MetricCard("Net Balance", "₹ 0.00", "Income minus expenses", "⚖", UITheme.COLOR_PRIMARY);
        savingsCard = new MetricCard("Total Savings", "₹ 0.00", "Accumulated goal savings", "★", UITheme.COLOR_TEAL);
        budgetCard = new MetricCard("Monthly Budget", "₹ 0.00", "0% utilized", "◔", UITheme.COLOR_WARNING);

        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);
        HBox.setHgrow(savingsCard, Priority.ALWAYS);
        HBox.setHgrow(budgetCard, Priority.ALWAYS);

        kpiContainer.getChildren().addAll(incomeCard, expenseCard, balanceCard, savingsCard, budgetCard);

        // 2. Interactive Charts Row 1: Pie Chart & Bar Chart
        HBox chartsRow1 = new HBox(20);

        // Category Pie Chart Card
        VBox pieCard = new VBox(12);
        pieCard.setPadding(new Insets(18));
        pieCard.setPrefWidth(450);
        UITheme.applyCardStyle(pieCard);
        Label pieTitle = new Label("Expense Distribution by Category");
        pieTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        categoryPieChart = new PieChart();
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);
        categoryPieChart.setPrefHeight(280);
        pieCard.getChildren().addAll(pieTitle, categoryPieChart);

        // Income vs Expense Comparison Bar Chart Card
        VBox barCard = new VBox(12);
        barCard.setPadding(new Insets(18));
        HBox.setHgrow(barCard, Priority.ALWAYS);
        UITheme.applyCardStyle(barCard);
        Label barTitle = new Label("Monthly Income vs. Expenses");
        barTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        CategoryAxis barXAxis = new CategoryAxis();
        NumberAxis barYAxis = new NumberAxis();
        barYAxis.setLabel("Amount (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + ")");

        incomeExpenseBarChart = new BarChart<>(barXAxis, barYAxis);
        incomeExpenseBarChart.setLegendVisible(true);
        incomeExpenseBarChart.setAnimated(false);
        incomeExpenseBarChart.setPrefHeight(280);
        barCard.getChildren().addAll(barTitle, incomeExpenseBarChart);

        chartsRow1.getChildren().addAll(pieCard, barCard);

        // 3. Interactive Charts Row 2: Spending Trends Line Chart
        VBox lineCard = new VBox(12);
        lineCard.setPadding(new Insets(18));
        UITheme.applyCardStyle(lineCard);
        Label lineTitle = new Label("Spending Trends Over Time (Daily Outflows)");
        lineTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        CategoryAxis lineXAxis = new CategoryAxis();
        lineXAxis.setLabel("Date");
        NumberAxis lineYAxis = new NumberAxis();
        lineYAxis.setLabel("Daily Spent");

        spendingTrendLineChart = new LineChart<>(lineXAxis, lineYAxis);
        spendingTrendLineChart.setLegendVisible(false);
        spendingTrendLineChart.setAnimated(false);
        spendingTrendLineChart.setPrefHeight(240);
        lineCard.getChildren().addAll(lineTitle, spendingTrendLineChart);

        // 4. Bottom Row: Recent Transactions Table & Budget Utilization Bars
        HBox bottomRow = new HBox(20);

        // Recent Transactions Card
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(18));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        UITheme.applyCardStyle(tableCard);

        Label tableTitle = new Label("Recent Transactions");
        tableTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        recentTransactionsTable = createRecentTransactionsTable();
        tableCard.getChildren().addAll(tableTitle, recentTransactionsTable);

        // Active Budgets Quick Card
        VBox budgetsCard = new VBox(12);
        budgetsCard.setPadding(new Insets(18));
        budgetsCard.setPrefWidth(380);
        UITheme.applyCardStyle(budgetsCard);

        Label budgetsTitle = new Label("Active Budget Status");
        budgetsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        budgetProgressContainer = new VBox(14);
        budgetsCard.getChildren().addAll(budgetsTitle, budgetProgressContainer);

        bottomRow.getChildren().addAll(tableCard, budgetsCard);

        getChildren().addAll(welcomeBox, kpiContainer, chartsRow1, lineCard, bottomRow);

        refreshData();
    }

    private TableView<Transaction> createRecentTransactionsTable() {
        TableView<Transaction> table = new TableView<>();
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(90);
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransactionType().getDisplayName()));

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category / Source");
        catCol.setPrefWidth(140);
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryOrSource()));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(180);
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatDate(data.getValue().getTransactionDate())));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(110);
        amountCol.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String prefix = t.getTransactionType() == TransactionType.INCOME ? "+ " : "- ";
            return new SimpleStringProperty(prefix + CurrencyFormatter.format(t.getAmount()));
        });

        table.getColumns().addAll(typeCol, catCol, descCol, dateCol, amountCol);
        return table;
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // 1. Fetch live metrics
        FinancialSummary summary = transactionService.calculateSummary(user.getId(), month, year);

        incomeCard.setValue(CurrencyFormatter.format(summary.getTotalIncome()));
        expenseCard.setValue(CurrencyFormatter.format(summary.getTotalExpenses()));

        BigDecimal netBalance = summary.getCurrentBalance();
        balanceCard.setValue(CurrencyFormatter.format(netBalance));
        if (netBalance.compareTo(BigDecimal.ZERO) >= 0) {
            balanceCard.setValueColor(UITheme.COLOR_SUCCESS);
            balanceCard.setSubtitle("Positive monthly cash flow");
        } else {
            balanceCard.setValueColor(UITheme.COLOR_DANGER);
            balanceCard.setSubtitle("Cash deficit this month");
        }

        savingsCard.setValue(CurrencyFormatter.format(summary.getTotalSavings()));

        budgetCard.setValue(CurrencyFormatter.format(summary.getMonthlyBudget()));
        budgetCard.setSubtitle(String.format("%.1f%% utilized", summary.getBudgetUtilization()));

        // 2. Refresh Pie Chart (Category Distribution)
        Map<String, BigDecimal> categorySpending = expenseService.getSpendingByCategory(user.getId(), month, year);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categorySpending.forEach((cat, amt) -> {
            if (amt.compareTo(BigDecimal.ZERO) > 0) {
                pieData.add(new PieChart.Data(cat + " (" + CurrencyFormatter.format(amt) + ")", amt.doubleValue()));
            }
        });
        categoryPieChart.setData(pieData);

        // 3. Refresh Bar Chart (Monthly Income vs Expense)
        incomeExpenseBarChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        String currentMonthName = DateUtil.getMonthName(month).substring(0, 3);
        incomeSeries.getData().add(new XYChart.Data<>(currentMonthName, summary.getTotalIncome()));
        expenseSeries.getData().add(new XYChart.Data<>(currentMonthName, summary.getTotalExpenses()));

        incomeExpenseBarChart.getData().addAll(incomeSeries, expenseSeries);

        // 4. Refresh Line Chart (Daily Spending Trends)
        LocalDate startOfMonth = DateUtil.getStartOfCurrentMonth();
        Map<LocalDate, BigDecimal> dailyTrends = expenseService.getDailySpendingTrends(user.getId(), startOfMonth, now);

        spendingTrendLineChart.getData().clear();
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Daily Spending");

        dailyTrends.forEach((date, amt) -> {
            trendSeries.getData().add(new XYChart.Data<>(String.valueOf(date.getDayOfMonth()), amt));
        });
        spendingTrendLineChart.getData().add(trendSeries);

        // 5. Refresh Recent Transactions Table
        List<Transaction> recent = transactionService.getRecentTransactions(user.getId(), 8);
        recentTransactionsTable.setItems(FXCollections.observableArrayList(recent));

        // 6. Refresh Budget Utilization Progress Bars
        budgetProgressContainer.getChildren().clear();
        List<Budget> budgets = budgetService.getBudgetsForPeriod(user.getId(), month, year);
        if (budgets.isEmpty()) {
            Label noBudgets = new Label("No category budgets set for this month.");
            noBudgets.setStyle("-fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
            budgetProgressContainer.getChildren().add(noBudgets);
        } else {
            for (Budget b : budgets) {
                ProgressBarWidget bar = new ProgressBarWidget(
                        b.getCategoryName() + ": " + CurrencyFormatter.format(b.getSpentAmount()) + " / " + CurrencyFormatter.format(b.getMonthlyAmount()),
                        b.getUtilizationPercentage(),
                        true
                );
                budgetProgressContainer.getChildren().add(bar);
            }
        }
    }
}

