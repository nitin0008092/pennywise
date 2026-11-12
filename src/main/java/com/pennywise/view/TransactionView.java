package com.pennywise.view;

import com.pennywise.enums.TransactionType;
import com.pennywise.model.Transaction;
import com.pennywise.model.User;
import com.pennywise.service.SessionManager;
import com.pennywise.service.TransactionService;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CsvExporter;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.UITheme;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * Unified transaction ledger view displaying both Income and Expense transactions polymorphically.
 */
public class TransactionView extends VBox {

    private final TransactionService transactionService;

    private TableView<Transaction> tableView;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Label summaryLabel;

    public TransactionView(TransactionService transactionService) {
        this.transactionService = transactionService;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Unified Transaction History");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        summaryLabel = new Label("Showing all recorded financial transactions");
        summaryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + ";");
        titleBox.getChildren().addAll(title, summaryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("⤓ Export to CSV");
        UITheme.applyPrimaryButton(exportBtn);
        exportBtn.setOnAction(e -> handleExportCsv());

        topBar.getChildren().addAll(titleBox, spacer, exportBtn);

        // Filter Bar Card
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14));
        UITheme.applyCardStyle(filterBar);

        searchField = new TextField();
        searchField.setPromptText("Search description, category, method...");
        searchField.setPrefWidth(240);
        searchField.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 6px; -fx-border-color: " + UITheme.COLOR_BORDER + "; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());

        typeFilter = new ComboBox<>(FXCollections.observableArrayList("All Types", "Income Only", "Expense Only"));
        typeFilter.setValue("All Types");
        typeFilter.setOnAction(e -> applyFilters());

        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("From Date");
        fromDatePicker.setPrefWidth(130);
        fromDatePicker.setOnAction(e -> applyFilters());

        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("To Date");
        toDatePicker.setPrefWidth(130);
        toDatePicker.setOnAction(e -> applyFilters());

        Button resetBtn = new Button("Clear Filters");
        UITheme.applySecondaryButton(resetBtn);
        resetBtn.setOnAction(e -> {
            searchField.clear();
            typeFilter.setValue("All Types");
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            refreshData();
        });

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, typeFilter, fromDatePicker, toDatePicker, resetBtn);

        // Transactions Table Container
        VBox tableContainer = new VBox();
        UITheme.applyCardStyle(tableContainer);
        tableContainer.setPadding(new Insets(14));

        tableView = createTable();
        tableContainer.getChildren().add(tableView);

        getChildren().addAll(topBar, filterBar, tableContainer);

        refreshData();
    }

    private TableView<Transaction> createTable() {
        TableView<Transaction> table = new TableView<>();
        table.setPrefHeight(480);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> idCol = new TableColumn<>("#");
        idCol.setPrefWidth(50);
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Transaction, Void> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(110);
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    String color = t.getTransactionType() == TransactionType.INCOME ? UITheme.COLOR_SUCCESS : UITheme.COLOR_DANGER;
                    setGraphic(IconHelper.createBadge(t.getTransactionType().getDisplayName(), color));
                }
            }
        });

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getTransactionDate())));

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category / Source");
        catCol.setPrefWidth(160);
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryOrSource()));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(220);
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Transaction, String> methodCol = new TableColumn<>("Payment Method");
        methodCol.setPrefWidth(130);
        methodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentMethod().getDisplayName()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(140);
        amountCol.setCellValueFactory(d -> {
            Transaction t = d.getValue();
            String prefix = t.getTransactionType() == TransactionType.INCOME ? "+ " : "- ";
            return new SimpleStringProperty(prefix + CurrencyFormatter.format(t.getAmount()));
        });
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    boolean isIncome = item.startsWith("+");
                    setStyle("-fx-text-fill: " + (isIncome ? UITheme.COLOR_SUCCESS : UITheme.COLOR_DANGER) + "; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(idCol, typeCol, dateCol, catCol, descCol, methodCol, amountCol);
        return table;
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        List<Transaction> list = transactionService.getAllTransactions(user.getId());
        tableView.setItems(FXCollections.observableArrayList(list));
        updateSummary(list);
    }

    private void applyFilters() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String query = searchField.getText();
        String typeSel = typeFilter.getValue();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        TransactionType type = null;
        if ("Income Only".equals(typeSel)) {
            type = TransactionType.INCOME;
        } else if ("Expense Only".equals(typeSel)) {
            type = TransactionType.EXPENSE;
        }

        List<Transaction> all = transactionService.getAllTransactions(user.getId());
        List<Transaction> filtered = transactionService.filter(all, query, from, to, type);

        tableView.setItems(FXCollections.observableArrayList(filtered));
        updateSummary(filtered);
    }

    private void updateSummary(List<Transaction> list) {
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;

        for (Transaction t : list) {
            if (t.getTransactionType() == TransactionType.INCOME) {
                totalIn = totalIn.add(t.getAmount());
            } else {
                totalOut = totalOut.add(t.getAmount());
            }
        }

        BigDecimal net = totalIn.subtract(totalOut);
        summaryLabel.setText(String.format("Filtered: %d records | Inflow: %s | Outflow: %s | Net: %s",
                list.size(),
                CurrencyFormatter.format(totalIn),
                CurrencyFormatter.format(totalOut),
                CurrencyFormatter.formatSigned(net, CurrencyFormatter.getDefaultCurrency())));
    }

    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions to CSV");
        fileChooser.setInitialFileName("pennywise_transactions_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                CsvExporter.exportTransactions(tableView.getItems(), file);
                AlertUtil.showInfo("Export Successful", "Transactions exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                AlertUtil.showError("Export Error", "Could not export transactions to file: " + ex.getMessage());
            }
        }
    }
}

