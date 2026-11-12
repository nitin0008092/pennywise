package com.pennywise.service;

import com.pennywise.enums.GoalStatus;
import com.pennywise.enums.NotificationType;
import com.pennywise.model.Budget;
import com.pennywise.model.Notification;
import com.pennywise.model.SavingsGoal;
import com.pennywise.repository.BudgetRepository;
import com.pennywise.repository.BudgetRepositoryImpl;
import com.pennywise.repository.ExpenseRepository;
import com.pennywise.repository.ExpenseRepositoryImpl;
import com.pennywise.repository.IncomeRepository;
import com.pennywise.repository.IncomeRepositoryImpl;
import com.pennywise.repository.NotificationRepository;
import com.pennywise.repository.NotificationRepositoryImpl;
import com.pennywise.repository.SavingsGoalRepository;
import com.pennywise.repository.SavingsGoalRepositoryImpl;
import com.pennywise.util.CurrencyFormatter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates rule-based financial insights and notifications based on live data.
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BudgetRepository budgetRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public NotificationServiceImpl() {
        this.notificationRepository = new NotificationRepositoryImpl();
        this.budgetRepository = new BudgetRepositoryImpl();
        this.incomeRepository = new IncomeRepositoryImpl();
        this.expenseRepository = new ExpenseRepositoryImpl();
        this.savingsGoalRepository = new SavingsGoalRepositoryImpl();
    }

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   BudgetRepository budgetRepository,
                                   IncomeRepository incomeRepository,
                                   ExpenseRepository expenseRepository,
                                   SavingsGoalRepository savingsGoalRepository) {
        this.notificationRepository = notificationRepository;
        this.budgetRepository = budgetRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Rule-based engine analyzing financial records and generating notifications.
     */
    @Override
    public List<Notification> generateInsights(Long userId) {
        List<Notification> insights = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // Rule 1: Cash Flow Deficit (Expenses > Income in current month)
        BigDecimal monthlyIncome = incomeRepository.getTotalIncomeByUserIdAndMonth(userId, month, year);
        BigDecimal monthlyExpense = expenseRepository.getTotalExpenseByUserIdAndMonth(userId, month, year);

        if (monthlyExpense.compareTo(monthlyIncome) > 0 && monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal deficit = monthlyExpense.subtract(monthlyIncome);
            insights.add(new Notification(
                    userId,
                    "Cash Flow Deficit Alert",
                    String.format("Your expenses this month (%s) exceed your income (%s) by %s.",
                            CurrencyFormatter.format(monthlyExpense),
                            CurrencyFormatter.format(monthlyIncome),
                            CurrencyFormatter.format(deficit)),
                    NotificationType.DANGER
            ));
        }

        // Rule 2: Budget Utilization Warnings
        List<Budget> budgets = budgetRepository.findByUserIdAndPeriod(userId, month, year);
        for (Budget b : budgets) {
            double util = b.getUtilizationPercentage();
            if (util >= 100.0) {
                insights.add(new Notification(
                        userId,
                        "Budget Exceeded",
                        String.format("You have exceeded your %s budget! Spent: %s (Limit: %s).",
                                b.getCategoryName(),
                                CurrencyFormatter.format(b.getSpentAmount()),
                                CurrencyFormatter.format(b.getMonthlyAmount())),
                        NotificationType.DANGER
                ));
            } else if (util >= 85.0) {
                insights.add(new Notification(
                        userId,
                        "Budget Approaching Limit",
                        String.format("You have used %.1f%% of your %s budget (%s of %s).",
                                util,
                                b.getCategoryName(),
                                CurrencyFormatter.format(b.getSpentAmount()),
                                CurrencyFormatter.format(b.getMonthlyAmount())),
                        NotificationType.WARNING
                ));
            }
        }

        // Rule 3: Goal Milestones Achieved
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        for (SavingsGoal g : goals) {
            if (g.isGoalReached() && g.getStatus() != GoalStatus.COMPLETED) {
                insights.add(new Notification(
                        userId,
                        "Savings Goal Achieved! 🎉",
                        String.format("Congratulations! You have reached your savings goal: '%s' (%s).",
                                g.getGoalName(), CurrencyFormatter.format(g.getTargetAmount())),
                        NotificationType.SUCCESS
                ));
            }
        }

        // Rule 4: Healthy Savings Rate
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal net = monthlyIncome.subtract(monthlyExpense);
            if (net.compareTo(BigDecimal.ZERO) > 0) {
                double rate = net.multiply(BigDecimal.valueOf(100))
                        .divide(monthlyIncome, 2, java.math.RoundingMode.HALF_UP).doubleValue();
                if (rate >= 20.0) {
                    insights.add(new Notification(
                            userId,
                            "Strong Savings Rate",
                            String.format("Great financial discipline! You have saved %.1f%% of your earnings this month.", rate),
                            NotificationType.INFO
                    ));
                }
            }
        }

        // Persist newly generated insights if not duplicates
        List<Notification> existing = notificationRepository.findByUserId(userId);
        for (Notification insight : insights) {
            boolean alreadyPresent = existing.stream().anyMatch(e -> e.getTitle().equalsIgnoreCase(insight.getTitle()));
            if (!alreadyPresent) {
                notificationRepository.save(insight);
            }
        }

        return notificationRepository.findByUserId(userId);
    }
}

