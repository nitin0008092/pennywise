package com.pennywise.view;

import com.pennywise.enums.IncomeSource;
import com.pennywise.enums.PaymentMethod;
import com.pennywise.enums.TransactionType;
import com.pennywise.exception.PennyWiseException;
import com.pennywise.model.Income;
import com.pennywise.model.User;
import com.pennywise.service.IncomeService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.CurrencyFormatter;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.UITheme;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * Income management screen supporting full CRUD, filtering, searching, and source categorization.
 */
public class IncomeView extends VBox {

    private final IncomeService incomeService;
    private final Runnable onDataChanged;

    private TableView<Income> tableView;
    private TextField searchField;
    private ComboBox<String> sourceFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Label totalIncomeLabel;

    public IncomeView(IncomeService incomeService, Runnable onDataChanged) {
        this.incomeService = incomeService;
        this.onDataChanged = onDataChanged;

        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Income Records");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");
        totalIncomeLabel = new Label("Total: ₹ 0.00");
        totalIncomeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UITheme.COLOR_SUCCESS + "; -fx-font-weight: bold;");
        titleBox.getChildren().addAll(title, totalIncomeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addIncomeBtn = new Button("+ Add New Income");
        UITheme.applyPrimaryButton(addIncomeBtn);
        addIncomeBtn.setOnAction(e -> showAddEditDialog(null));

        topBar.getChildren().addAll(titleBox, spacer, addIncomeBtn);

        // Filter Bar Card
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14));
        UITheme.applyCardStyle(filterBar);

        searchField = new TextField();
        searchField.setPromptText("Search description, source...");
        searchField.setPrefWidth(220);
        searchField.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 6px; -fx-border-color: " + UITheme.COLOR_BORDER + "; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());

        sourceFilter = new ComboBox<>();
        sourceFilter.getItems().add("All Sources");
        for (IncomeSource s : IncomeSource.values()) {
            sourceFilter.getItems().add(s.getDisplayName());
        }
        sourceFilter.setValue("All Sources");
        sourceFilter.setStyle("-fx-padding: 4px;");
        sourceFilter.setOnAction(e -> applyFilters());

        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("From Date");
        fromDatePicker.setPrefWidth(140);
        fromDatePicker.setOnAction(e -> applyFilters());

        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("To Date");
        toDatePicker.setPrefWidth(140);
        toDatePicker.setOnAction(e -> applyFilters());

        Button resetBtn = new Button("Clear Filters");
        UITheme.applySecondaryButton(resetBtn);
        resetBtn.setOnAction(e -> {
            searchField.clear();
            sourceFilter.setValue("All Sources");
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            refreshData();
        });

        filterBar.getChildren().addAll(new Label("Filter:"), searchField, sourceFilter, fromDatePicker, toDatePicker, resetBtn);

        // Income Table
        VBox tableContainer = new VBox();
        UITheme.applyCardStyle(tableContainer);
        tableContainer.setPadding(new Insets(14));

        tableView = createIncomeTable();
        tableContainer.getChildren().add(tableView);

        getChildren().addAll(topBar, filterBar, tableContainer);

        refreshData();
    }

    private TableView<Income> createIncomeTable() {
        TableView<Income> table = new TableView<>();
        table.setPrefHeight(450);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Income, String> idCol = new TableColumn<>("#");
        idCol.setPrefWidth(50);
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Income, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(DateUtil.formatDate(d.getValue().getTransactionDate())));

        TableColumn<Income, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setPrefWidth(130);
        sourceCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSource().getDisplayName()));

        TableColumn<Income, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(200);
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Income, String> methodCol = new TableColumn<>("Payment Method");
        methodCol.setPrefWidth(130);
        methodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentMethod().getDisplayName()));

        TableColumn<Income, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(130);
        amountCol.setCellValueFactory(d -> new SimpleStringProperty("+ " + CurrencyFormatter.format(d.getValue().getAmount())));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + UITheme.COLOR_SUCCESS + "; -fx-font-weight: bold;");
                }
            }
        });

        // Actions Column (Edit / Delete)
        TableColumn<Income, Void> actionCol = new TableColumn<>("Actions");
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
                    Income item = getTableView().getItems().get(getIndex());
                    showAddEditDialog(item);
                });

                deleteBtn.setOnAction(e -> {
                    Income item = getTableView().getItems().get(getIndex());
                    if (AlertUtil.showConfirmation("Delete Income", "Are you sure you want to delete this income entry of " + CurrencyFormatter.format(item.getAmount()) + "?")) {
                        User user = SessionManager.getInstance().getCurrentUser();
                        incomeService.deleteIncome(item.getId(), user.getId());
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

        table.getColumns().addAll(idCol, dateCol, sourceCol, descCol, methodCol, amountCol, actionCol);
        return table;
    }

    public void refreshData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        List<Income> list = incomeService.getIncomeHistory(user.getId());
        tableView.setItems(FXCollections.observableArrayList(list));

        BigDecimal total = list.stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalIncomeLabel.setText("Total Income: " + CurrencyFormatter.format(total));
    }

    private void applyFilters() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String query = searchField.getText();
        String sourceSel = sourceFilter.getValue();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        List<Income> all = incomeService.getIncomeHistory(user.getId());

        List<Income> filtered = all.stream().filter(i -> {
            if (sourceSel != null && !sourceSel.equals("All Sources") && !i.getSource().getDisplayName().equalsIgnoreCase(sourceSel)) {
                return false;
            }
            if (from != null && i.getTransactionDate().isBefore(from)) {
                return false;
            }
            if (to != null && i.getTransactionDate().isAfter(to)) {
                return false;
            }
            if (query != null && !query.isBlank()) {
                String q = query.trim().toLowerCase();
                boolean matchDesc = i.getDescription() != null && i.getDescription().toLowerCase().contains(q);
                boolean matchSrc = i.getSource().getDisplayName().toLowerCase().contains(q);
                boolean matchAmt = i.getAmount().toPlainString().contains(q);
                return matchDesc || matchSrc || matchAmt;
            }
            return true;
        }).toList();

        tableView.setItems(FXCollections.observableArrayList(filtered));
        BigDecimal filteredTotal = filtered.stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalIncomeLabel.setText("Filtered Total: " + CurrencyFormatter.format(filteredTotal) + " (" + filtered.size() + " records)");
    }

    private void showAddEditDialog(Income existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Income" : "Edit Income");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label(existing == null ? "Record New Income" : "Edit Income Record");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        TextField amountField = new TextField(existing != null ? existing.getAmount().toPlainString() : "");
        amountField.setPromptText("e.g. 50000.00");

        ComboBox<IncomeSource> sourceBox = new ComboBox<>(FXCollections.observableArrayList(IncomeSource.values()));
        sourceBox.setValue(existing != null ? existing.getSource() : IncomeSource.SALARY);
        sourceBox.setMaxWidth(Double.MAX_VALUE);

        TextField descField = new TextField(existing != null ? existing.getDescription() : "");
        descField.setPromptText("e.g. Monthly corporate salary");

        DatePicker datePicker = new DatePicker(existing != null ? existing.getTransactionDate() : LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        ComboBox<PaymentMethod> methodBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        methodBox.setValue(existing != null ? existing.getPaymentMethod() : PaymentMethod.BANK_TRANSFER);
        methodBox.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Amount (" + CurrencyFormatter.getDefaultCurrency().getSymbol() + "):*"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Income Source:*"), 0, 1);
        grid.add(sourceBox, 1, 1);
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

        Button saveBtn = new Button(existing == null ? "Save Income" : "Update");
        UITheme.applyPrimaryButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                User user = SessionManager.getInstance().getCurrentUser();

                if (existing == null) {
                    Income newIncome = new Income(user.getId(), amt, sourceBox.getValue(),
                            descField.getText().trim(), datePicker.getValue(), methodBox.getValue());
                    incomeService.addIncome(newIncome);
                } else {
                    existing.setAmount(amt);
                    existing.setSource(sourceBox.getValue());
                    existing.setDescription(descField.getText().trim());
                    existing.setTransactionDate(datePicker.getValue());
                    existing.setPaymentMethod(methodBox.getValue());
                    incomeService.updateIncome(existing);
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

