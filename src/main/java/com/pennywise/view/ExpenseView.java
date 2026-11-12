package com.pennywise.view;

import com.pennywise.enums.PaymentMethod;
import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.Expense;
import com.pennywise.model.ExpenseCategory;
import com.pennywise.model.User;
import com.pennywise.service.ExpenseService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Expense management screen supporting CRUD, category filtering, search, and sorting.
 */
public class ExpenseView extends VBox {

    private final ExpenseService expenseService;
    private final Runnable onDataChanged;

    private TableView<Expense> tableView;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> sortFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Label totalExpenseLabel;

    public ExpenseView(ExpenseService expenseService, Runnable onDataChanged) {
        this.expenseService = expenseService;
        this.onDataChanged = onDataChanged;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Expense Records");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        totalExpenseLabel = new Label("Total: ₹ 0.00");
        totalExpenseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-weight: bold;");
        titleBox.getChildren().addAll(title, totalExpenseLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addExpenseBtn = new Button("+ Add New Expense");
        UITheme.applyPrimaryButton(addExpenseBtn);
        addExpenseBtn.setOnAction(e -> showAddEditDialog(null));

        topBar.getChildren().addAll(titleBox, spacer, addExpenseBtn);

        // Filter & Sort Bar Card
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14));
        UITheme.applyCardStyle(filterBar);

        searchField = new TextField();
        searchField.setPromptText("Search description, category...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 6px; -fx-border-color: " + UITheme.COLOR_BORDER + "; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> applyFilters());

        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Date (Newest First)", "Date (Oldest First)", "Amount (High to Low)", "Amount (Low to High)");
        sortFilter.setValue("Date (Newest First)");
        sortFilter.setOnAction(e -> applyFilters());

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
            categoryFilter.setValue("All Categories");
            sortFilter.setValue("Date (Newest First)");
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            refreshData();
        });

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, categoryFilter, sortFilter, fromDatePicker, toDatePicker, resetBtn);

        // Expense Table
        VBox tableContainer = new VBox();
        UITheme.applyCardStyle(tableContainer);
        tableContainer.setPadding(new Insets(14));

        tableView = createExpenseTable();
        tableContainer.getChildren().add(tableView);

        getChildren().addAll(topBar, filterBar, tableContainer);

        refreshData();
    }

    private TableView<Expense> createExpenseTable() {
        TableView<Expense> table = new TableView<>();
        table.setPrefHeight(450);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, String> idCol = new TableColumn<>("#");
        idCol.setPrefWidth(50);
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getTransactionDate())));

        TableColumn<Expense, Void> catCol = new TableColumn<>("Category");
        catCol.setPrefWidth(150);
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Expense exp = getTableView().getItems().get(getIndex());
                    String name = exp.getCategory() != null ? exp.getCategory().getName() : "General";
                    String color = exp.getCategory() != null ? exp.getCategory().getColorCode() : "#2563EB";
                    setGraphic(IconHelper.createBadge(name, color));
                }
            }
        });

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(200);
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Expense, String> methodCol = new TableColumn<>("Payment Method");
        methodCol.setPrefWidth(130);
        methodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentMethod().getDisplayName()));

        TableColumn<Expense, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(130);
        amountCol.setCellValueFactory(d -> new SimpleStringProperty("- " + CurrencyFormatter.format(d.getValue().getAmount())));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-weight: bold;");
                }
            }
        });

        // Actions Column (Edit / Delete)
        TableColumn<Expense, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                UITheme.applySecondaryButton(editBtn);
                UITheme.applyDangerButton(deleteBtn);
                editBtn.setStyle(editBtn.getStyle() + "-fx-padding: 4px 8px; -fx-font-size: 11px;");
                deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-padding: 4px 8px; -fx-font-size: 11px;");

                editBtn.setOnAction(e -> {
                    Expense item = getTableView().getItems().get(getIndex());
                    showAddEditDialog(item);
                });

                deleteBtn.setOnAction(e -> {
                    Expense item = getTableView().getItems().get(getIndex());
                    if (AlertUtil.showConfirmation("Delete Expense", "Are you sure you want to delete this expense of " + CurrencyFormatter.format(item.getAmount()) + "?")) {
                        User user = SessionManager.getInstance().getCurrentUser();
                        expenseService.deleteExpense(item.getId(), user.getId());
                        refreshData();
                        onDataChanged.run();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(idCol, dateCol, catCol, descCol, methodCol, amountCol, actionCol);
        return table;
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Populate category dropdown
        List<ExpenseCategory> categories = expenseService.getAvailableCategories(user.getId());
        String currentSel = categoryFilter.getValue();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All Categories");
        for (ExpenseCategory c : categories) {
            categoryFilter.getItems().add(c.getName());
        }
        categoryFilter.setValue(currentSel != null && categoryFilter.getItems().contains(currentSel) ? currentSel : "All Categories");

        List<Expense> list = expenseService.getExpenseHistory(user.getId());
        tableView.setItems(FXCollections.observableArrayList(list));

        BigDecimal total = list.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpenseLabel.setText("Total Expenses: " + CurrencyFormatter.format(total));
    }

    private void applyFilters() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String query = searchField.getText();
        String catSel = categoryFilter.getValue();
        String sortOption = sortFilter.getValue();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        List<Expense> all = expenseService.getExpenseHistory(user.getId());

        List<Expense> filtered = all.stream().filter(e -> {
            if (catSel != null && !catSel.equals("All Categories")) {
                if (e.getCategory() == null || !e.getCategory().getName().equalsIgnoreCase(catSel)) {
                    return false;
                }
            }
            if (from != null && e.getTransactionDate().isBefore(from)) {
                return false;
            }
            if (to != null && e.getTransactionDate().isAfter(to)) {
                return false;
            }
            if (query != null && !query.isBlank()) {
                String q = query.trim().toLowerCase();
                boolean matchDesc = e.getDescription() != null && e.getDescription().toLowerCase().contains(q);
                boolean matchCat = e.getCategory() != null && e.getCategory().getName().toLowerCase().contains(q);
                boolean matchAmt = e.getAmount().toPlainString().contains(q);
                return matchDesc || matchCat || matchAmt;
            }
            return true;
        }).sorted((a, b) -> {
            if ("Date (Oldest First)".equals(sortOption)) {
                return a.getTransactionDate().compareTo(b.getTransactionDate());
            } else if ("Amount (High to Low)".equals(sortOption)) {
                return b.getAmount().compareTo(a.getAmount());
            } else if ("Amount (Low to High)".equals(sortOption)) {
                return a.getAmount().compareTo(b.getAmount());
            } else {
                return b.getTransactionDate().compareTo(a.getTransactionDate()); // Default: Date newest
            }
        }).toList();

        tableView.setItems(FXCollections.observableArrayList(filtered));
        BigDecimal filteredTotal = filtered.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpenseLabel.setText("Filtered Total: " + CurrencyFormatter.format(filteredTotal) + " (" + filtered.size() + " records)");
    }

    private void showAddEditDialog(Expense existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Expense" : "Edit Expense");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label(existing == null ? "Record New Expense" : "Edit Expense Record");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        User user = SessionManager.getInstance().getCurrentUser();
        List<ExpenseCategory> categories = expenseService.getAvailableCategories(user.getId());

        TextField amountField = new TextField(existing != null ? existing.getAmount().toPlainString() : "");
        amountField.setPromptText("e.g. 1500.00");

        ComboBox<ExpenseCategory> catBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        if (existing != null && existing.getCategory() != null) {
            catBox.setValue(existing.getCategory());
        } else if (!categories.isEmpty()) {
            catBox.setValue(categories.getFirst());
        }
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField descField = new TextField(existing != null ? existing.getDescription() : "");
        descField.setPromptText("e.g. Weekly grocery shopping");

        DatePicker datePicker = new DatePicker(existing != null ? existing.getTransactionDate() : LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        ComboBox<PaymentMethod> methodBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        methodBox.setValue(existing != null ? existing.getPaymentMethod() : PaymentMethod.UPI);
        methodBox.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Amount (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + "):*"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Category:*"), 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(new Label("Date:*"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Payment Method:*"), 0, 4);
        grid.add(methodBox, 1, 4);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: " + UITheme.COLOR_DANGER + "; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        UITheme.applySecondaryButton(cancelBtn);
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Save Expense" : "Update");
        UITheme.applyPrimaryButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                ExpenseCategory selectedCat = catBox.getValue();
                if (selectedCat == null) {
                    throw new PennyWiseException("Please select an expense category.");
                }

                if (existing == null) {
                    Expense newExpense = new Expense(user.getId(), selectedCat.getId(), amt,
                            descField.getText().trim(), datePicker.getValue(), methodBox.getValue());
                    expenseService.addExpense(newExpense);
                } else {
                    existing.setAmount(amt);
                    existing.setCategoryId(selectedCat.getId());
                    existing.setCategory(selectedCat);
                    existing.setDescription(descField.getText().trim());
                    existing.setTransactionDate(datePicker.getValue());
                    existing.setPaymentMethod(methodBox.getValue());
                    expenseService.updateExpense(existing);
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
}

