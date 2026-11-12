package com.pennywise.view;

import com.pennywise.enums.NotificationType;
import com.pennywise.model.Notification;
import com.pennywise.model.User;
import com.pennywise.service.AuthService;
import com.pennywise.service.NotificationService;
import com.pennywise.service.SessionManager;
import com.pennywise.util.AlertUtil;
import com.pennywise.util.DateUtil;
import com.pennywise.view.components.IconHelper;
import com.pennywise.view.components.UITheme;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Main application layout container with left sidebar, top bar, and dynamic content pane.
 */
public class MainLayoutView extends BorderPane {

    private final AuthService authService;
    private final NotificationService notificationService;
    private final Runnable onLogout;

    private final Map<String, Node> viewRegistry = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();

    private Label pageTitleLabel;
    private Label notificationBadge;
    private StackPane contentArea;
    private String currentView = "dashboard";

    public MainLayoutView(AuthService authService, NotificationService notificationService, Runnable onLogout) {
        this.authService = authService;
        this.notificationService = notificationService;
        this.onLogout = onLogout;

        setStyle("-fx-background-color: " + UITheme.COLOR_BG + ";");
        setLeft(buildSidebar());
        setTop(buildTopBar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(24));
        contentArea.setStyle("-fx-background-color: " + UITheme.COLOR_BG + ";");

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + UITheme.COLOR_BG + ";");
        setCenter(scrollPane);
    }

    public void registerView(String name, Node view) {
        viewRegistry.put(name.toLowerCase(), view);
    }

    public void navigateTo(String viewName) {
        String key = viewName.toLowerCase();
        Node view = viewRegistry.get(key);
        if (view != null) {
            currentView = key;
            contentArea.getChildren().setAll(view);

            // Update top bar title
            pageTitleLabel.setText(formatTitle(key));

            // Update sidebar active highlights
            for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    styleNavButtonActive(entry.getValue());
                } else {
                    styleNavButtonInactive(entry.getValue());
                }
            }
            refreshNotifications();
        }
    }

    private Node buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + UITheme.COLOR_NAVY + ";");
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setSpacing(6);

        // App Logo & Brand Header
        HBox brandBox = new HBox(12);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 8, 24, 8));

        Label logo = new Label("₹");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; " +
                      "-fx-background-color: " + UITheme.COLOR_PRIMARY + "; " +
                      "-fx-background-radius: 8px; -fx-padding: 4px 10px;");

        VBox titleBox = new VBox(2);
        Label appName = new Label("PennyWise");
        appName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label version = new Label("v1.0 • Desktop");
        version.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        titleBox.getChildren().addAll(appName, version);

        brandBox.getChildren().addAll(logo, titleBox);

        // Section Label
        Label menuHeader = new Label("MAIN MENU");
        menuHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748B; -fx-padding: 8px 12px 4px 12px;");

        // Navigation Items
        VBox navItems = new VBox(4);
        navItems.getChildren().addAll(
                menuHeader,
                createNavButton("dashboard", "Dashboard", IconHelper.ICON_DASHBOARD),
                createNavButton("transactions", "Transactions", IconHelper.ICON_TRANSACTIONS),
                createNavButton("income", "Income", IconHelper.ICON_INCOME),
                createNavButton("expenses", "Expenses", IconHelper.ICON_EXPENSE),
                createNavButton("budgets", "Budgets", IconHelper.ICON_BUDGET),
                createNavButton("savings", "Savings Goals", IconHelper.ICON_SAVINGS),
                createNavButton("reports", "Reports & Analytics", IconHelper.ICON_REPORTS),
                createNavButton("settings", "Settings", IconHelper.ICON_SETTINGS)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User Profile & Logout Bottom Card
        VBox userCard = new VBox(8);
        userCard.setStyle("-fx-background-color: " + UITheme.COLOR_NAVY_LIGHT + "; -fx-background-radius: 10px; -fx-padding: 12px;");

        User user = SessionManager.getInstance().getCurrentUser();
        String userName = user != null ? user.getFullName() : "User";
        String userEmail = user != null ? user.getEmail() : "";

        Label nameLbl = new Label(userName);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label emailLbl = new Label(userEmail);
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #F1F5F9; -fx-font-size: 12px; " +
                           "-fx-background-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            if (AlertUtil.showConfirmation("Sign Out", "Are you sure you want to sign out of PennyWise?")) {
                authService.logout();
                onLogout.run();
            }
        });

        userCard.getChildren().addAll(nameLbl, emailLbl, logoutBtn);

        sidebar.getChildren().addAll(brandBox, navItems, spacer, userCard);
        return sidebar;
    }

    private Button createNavButton(String key, String text, String icon) {
        Button btn = new Button(icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> navigateTo(key));
        navButtons.put(key, btn);
        styleNavButtonInactive(btn);
        return btn;
    }

    private void styleNavButtonActive(Button btn) {
        btn.setStyle("-fx-background-color: " + UITheme.COLOR_PRIMARY + "; " +
                     "-fx-text-fill: white; " +
                     "-fx-font-size: 13px; -fx-font-weight: bold; " +
                     "-fx-background-radius: 8px; -fx-padding: 10px 14px; -fx-cursor: hand;");
    }

    private void styleNavButtonInactive(Button btn) {
        String base = "-fx-background-color: transparent; " +
                      "-fx-text-fill: #94A3B8; " +
                      "-fx-font-size: 13px; -fx-font-weight: 500; " +
                      "-fx-background-radius: 8px; -fx-padding: 10px 14px; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + UITheme.COLOR_NAVY_LIGHT + "; -fx-text-fill: white;";

        btn.setStyle(base);
        btn.setOnMouseEntered(e -> {
            if (!btn.equals(navButtons.get(currentView))) {
                btn.setStyle(base + hover);
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.equals(navButtons.get(currentView))) {
                btn.setStyle(base);
            }
        });
    }

    private Node buildTopBar() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: " + UITheme.COLOR_BORDER + "; " +
                        "-fx-border-width: 0 0 1px 0; -fx-padding: 14px 24px;");

        pageTitleLabel = new Label("Dashboard");
        pageTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_TEXT_MAIN + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date Display Badge
        String dateStr = DateUtil.formatDate(LocalDate.now());
        Label dateLabel = new Label("📅  " + dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UITheme.COLOR_TEXT_MUTED + "; " +
                           "-fx-background-color: " + UITheme.COLOR_BG + "; " +
                           "-fx-padding: 6px 12px; -fx-background-radius: 6px;");

        // Notification Bell Button with Unread Badge
        StackPane notifButtonContainer = new StackPane();
        Button notifBtn = new Button("🔔");
        notifBtn.setStyle("-fx-background-color: " + UITheme.COLOR_BG + "; -fx-font-size: 15px; " +
                          "-fx-background-radius: 8px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        notifBtn.setOnAction(e -> showNotificationsModal());

        notificationBadge = new Label("0");
        notificationBadge.setStyle("-fx-background-color: " + UITheme.COLOR_DANGER + "; -fx-text-fill: white; " +
                                   "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1px 5px; -fx-background-radius: 8px;");
        StackPane.setAlignment(notificationBadge, Pos.TOP_RIGHT);
        notificationBadge.setMouseTransparent(true);
        notificationBadge.setVisible(false);

        notifButtonContainer.getChildren().addAll(notifBtn, notificationBadge);

        // Currency Indicator Pill
        User user = SessionManager.getInstance().getCurrentUser();
        String currCode = (user != null && user.getCurrency() != null) ? user.getCurrency().getCode() + " (" + user.getCurrency().getSymbol() + ")" : "INR (₹)";
        Label currencyPill = new Label("💰  " + currCode);
        currencyPill.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.COLOR_PRIMARY + "; " +
                              "-fx-background-color: #EFF6FF; -fx-padding: 6px 12px; -fx-background-radius: 6px;");

        topBar.getChildren().addAll(pageTitleLabel, spacer, dateLabel, currencyPill, notifButtonContainer);
        return topBar;
    }

    public void refreshNotifications() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            notificationService.generateInsights(user.getId());
            List<Notification> unread = notificationService.getUnreadNotifications(user.getId());
            if (!unread.isEmpty()) {
                notificationBadge.setText(String.valueOf(unread.size()));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        }
    }

    private void showNotificationsModal() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        List<Notification> all = notificationService.getNotificationsForUser(user.getId());
        NotificationDialog dialog = new NotificationDialog(all, () -> {
            notificationService.markAllAsRead(user.getId());
            refreshNotifications();
        });
        dialog.showAndWait();
        refreshNotifications();
    }

    private String formatTitle(String key) {
        return switch (key) {
            case "dashboard" -> "Financial Dashboard";
            case "transactions" -> "Transaction History";
            case "income" -> "Income Management";
            case "expenses" -> "Expense Management";
            case "budgets" -> "Monthly Budgeting";
            case "savings" -> "Savings Goals";
            case "reports" -> "Reports & Analytics";
            case "settings" -> "User Profile & Preferences";
            default -> "PennyWise";
        };
    }
}

