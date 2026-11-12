package com.pennywise.view;

import com.pennywise.model.FinancialSummary;
import com.pennywise.model.User;
import com.pennywise.service.ReportService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.MetricCard;
import com.pennywise.view.components.UITheme;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * Financial Reports and Analytics module providing monthly/annual summaries,
 * category breakdowns, month-to-month trend charts, and CSV exporting.
 */
public class ReportView extends VBox {

    private final ReportService reportService;

    private ComboBox<String> periodTypeCombo;
    private ComboBox<String> monthCombo;
    private ComboBox<Integer> yearCombo;

    private MetricCard incomeCard;
    private MetricCard expenseCard;
    private MetricCard savingsCard;
    private MetricCard savingsRateCard;
    private MetricCard topCategoryCard;
    private MetricCard avgDailyCard;

    private BarChart<String, Number> monthlyComparisonChart;
    private PieChart categoryPieChart;

    public ReportView(ReportService reportService) {
        this.reportService = reportService;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Financial Reports & Analytics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        Label subtitle = new Label("Comprehensive income, expense, and savings analytics");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        periodTypeCombo = new ComboBox<>(FXCollections.observableArrayList("Monthly Report", "Annual Report"));
        periodTypeCombo.setValue("Monthly Report");
        periodTypeCombo.setOnAction(e -> {
            boolean isMonthly = "Monthly Report".equals(periodTypeCombo.getValue());
            monthCombo.setVisible(isMonthly);
            refreshData();
        });

        monthCombo = new ComboBox<>();
        for (int m = 1; m <= 12; m++) {
            monthCombo.getItems().add(DateUtil.getMonthName(m));
        }
        monthCombo.setValue(DateUtil.getMonthName(LocalDate.now().getMonthValue()));
        monthCombo.setOnAction(e -> refreshData());

        yearCombo = new ComboBox<>(FXCollections.observableArrayList(2024, 2025, 2026, 2027, 2028));
        yearCombo.setValue(LocalDate.now().getYear());
        yearCombo.setOnAction(e -> refreshData());

        Button exportBtn = new Button("⤓ Export Report CSV");
        UITheme.applyPrimaryButton(exportBtn);
        exportBtn.setOnAction(e -> handleExportCsv());

        topBar.getChildren().addAll(titleBox, spacer, periodTypeCombo, monthCombo, yearCombo, exportBtn);

        // KPI Summary Cards Grid
        HBox kpiRow1 = new HBox(16);
        incomeCard = new MetricCard("Total Inflow", "₹ 0.00", "Total income recorded", "⇣", UITheme.COLOR_SUCCESS);
        expenseCard = new MetricCard("Total Outflow", "₹ 0.00", "Total expenditures", "⇡", UITheme.COLOR_DANGER);
        savingsCard = new MetricCard("Net Savings", "₹ 0.00", "Income minus expenses", "⚖", UITheme.COLOR_PRIMARY);

        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(savingsCard, Priority.ALWAYS);
        kpiRow1.getChildren().addAll(incomeCard, expenseCard, savingsCard);

        HBox kpiRow2 = new HBox(16);
        savingsRateCard = new MetricCard("Savings Rate", "0.0%", "% of income saved", "%", UITheme.COLOR_TEAL);
        topCategoryCard = new MetricCard("Top Spending Category", "None", "Highest expenditure driver", "▲", UITheme.COLOR_WARNING);
        avgDailyCard = new MetricCard("Average Daily Spending", "₹ 0.00", "Normalized daily cost", "📅", UITheme.COLOR_PRIMARY);

        HBox.setHgrow(savingsRateCard, Priority.ALWAYS);
        HBox.setHgrow(topCategoryCard, Priority.ALWAYS);
        HBox.setHgrow(avgDailyCard, Priority.ALWAYS);
        kpiRow2.getChildren().addAll(savingsRateCard, topCategoryCard, avgDailyCard);

        // Charts Row
        HBox chartsRow = new HBox(20);

        // Month-to-Month comparison chart
        VBox barCard = new VBox(12);
        barCard.setPadding(new Insets(18));
        HBox.setHgrow(barCard, Priority.ALWAYS);
        UITheme.applyCardStyle(barCard);

        Label barTitle = new Label("Month-to-Month Spending Trend");
        barTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + ")");

        monthlyComparisonChart = new BarChart<>(xAxis, yAxis);
        monthlyComparisonChart.setLegendVisible(false);
        monthlyComparisonChart.setAnimated(false);
        monthlyComparisonChart.setPrefHeight(300);
        barCard.getChildren().addAll(barTitle, monthlyComparisonChart);

        // Category Breakdown Pie Chart
        VBox pieCard = new VBox(12);
        pieCard.setPadding(new Insets(18));
        pieCard.setPrefWidth(420);
        UITheme.applyCardStyle(pieCard);

        Label pieTitle = new Label("Category Breakdown");
        pieTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        categoryPieChart = new PieChart();
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);
        categoryPieChart.setPrefHeight(300);
        pieCard.getChildren().addAll(pieTitle, categoryPieChart);

        chartsRow.getChildren().addAll(barCard, pieCard);

        getChildren().addAll(topBar, kpiRow1, kpiRow2, chartsRow);

        refreshData();
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        boolean isMonthly = "Monthly Report".equals(periodTypeCombo.getValue());
        int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
        int month = getSelectedMonth();

        FinancialSummary summary = isMonthly ?
                reportService.getMonthlyReport(user.getId(), month, year) :
                reportService.getAnnualReport(user.getId(), year);

        incomeCard.setValue(CurrencyFormatter.format(summary.getTotalIncome()));
        expenseCard.setValue(CurrencyFormatter.format(summary.getTotalExpenses()));

        BigDecimal net = summary.getCurrentBalance();
        savingsCard.setValue(CurrencyFormatter.format(net));
        savingsCard.setValueColor(net.compareTo(BigDecimal.ZERO) >= 0 ? UITheme.COLOR_SUCCESS : UITheme.COLOR_DANGER);

        savingsRateCard.setValue(String.format("%.1f%%", summary.getSavingsRate()));
        topCategoryCard.setValue(summary.getHighestSpendingCategory());
        topCategoryCard.setSubtitle(CurrencyFormatter.format(summary.getHighestCategoryAmount()) + " spent");

        avgDailyCard.setValue(CurrencyFormatter.format(summary.getAverageDailySpending()));

        // Populate Month-to-Month Bar Chart
        monthlyComparisonChart.getData().clear();
        Map<String, BigDecimal> monthMap = reportService.getMonthToMonthComparison(user.getId(), year);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        monthMap.forEach((m, amt) -> series.getData().add(new XYChart.Data<>(m, amt)));
        monthlyComparisonChart.getData().add(series);

        // Populate Category Breakdown Pie Chart
        Map<String, BigDecimal> catBreakdown = reportService.getCategoryBreakdown(user.getId(), month, year);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        catBreakdown.forEach((cat, amt) -> {
            if (amt.compareTo(BigDecimal.ZERO) > 0) {
                pieData.add(new PieChart.Data(cat + " (" + CurrencyFormatter.format(amt) + ")", amt.doubleValue()));
            }
        });
        categoryPieChart.setData(pieData);
    }

    private void handleExportCsv() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Full Financial Report");
        fileChooser.setInitialFileName("pennywise_financial_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                reportService.exportReportToCsv(user.getId(), file);
                AlertUtil.showInfo("Export Successful", "Financial report successfully exported to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                AlertUtil.showError("Export Error", "Could not export report: " + ex.getMessage());
            }
        }
    }

    private int getSelectedMonth() {
        String mName = monthCombo.getValue();
        for (int i = 1; i <= 12; i++) {
            if (DateUtil.getMonthName(i).equalsIgnoreCase(mName)) {
                return i;
            }
        }
        return LocalDate.now().getMonthValue();
    }
}

