# PennyWise – Object-Oriented Programming (OOP) Implementation Guide

This document provides a comprehensive mapping of Object-Oriented Programming concepts implemented in **PennyWise**, specifically structured to aid undergraduate viva and academic evaluation.

---

## 1. The Four Pillars of OOP

### 1.1 Encapsulation
* **Definition**: Bundling data (attributes) and methods that operate on that data into a single unit, while restricting direct access to object internals.
* **Implementation in PennyWise**:
  - All domain model classes (`User`, `Transaction`, `Income`, `Expense`, `Budget`, `SavingsGoal`, `SavingsContribution`) have strictly `private` attributes.
  - Controlled read/write access is provided exclusively via getters and setters with internal invariants.
  - Sensitive information such as passwords is never stored as plain text; salted hashes are encapsulated inside `User` and handled via `PasswordHasher`.
  - Immutable fields (such as `id` once persisted, or `createdAt` timestamps) do not expose mutating public setters.

### 1.2 Inheritance
* **Definition**: Mechanism where a new class derives properties and behaviors from an existing superclass, fostering code reuse and taxonomic hierarchies.
* **Implementation in PennyWise**:
  - `public abstract class Transaction`: Base abstract superclass holding common transaction attributes:
    - `id`, `userId`, `amount`, `description`, `transactionDate`, `paymentMethod`, `createdAt`.
  - `public class Income extends Transaction`: Specializes `Transaction` by adding an `IncomeSource` attribute and source-specific calculations.
  - `public class Expense extends Transaction`: Specializes `Transaction` by binding to an `ExpenseCategory` entity with category limit validation.
  - Custom exceptions inherit from Java standard exceptions:
    - `PennyWiseException extends RuntimeException`
    - `DatabaseException extends PennyWiseException`
    - `AuthenticationException extends PennyWiseException`
    - `ValidationException extends PennyWiseException`

### 1.3 Polymorphism
* **Definition**: The ability of different classes to respond to the same message/method call in distinct, class-specific ways.
* **Implementation in PennyWise**:
  - **Dynamic Method Overriding (Runtime Polymorphism)**:
    - `abstract String getTransactionType()` in `Transaction`:
      - `Income.getTransactionType()` returns `"INCOME"`.
      - `Expense.getTransactionType()` returns `"EXPENSE"`.
    - `abstract BigDecimal getEffectiveImpact()` in `Transaction`:
      - Overridden in `Income` to return `+amount` (positive cash flow).
      - Overridden in `Expense` to return `-amount` (negative cash flow).
    - Overridden `toString()`, `equals()`, and `hashCode()` methods across models.
  - **Static Method Overloading (Compile-Time Polymorphism)**:
    - `TransactionService.filterTransactions(List<Transaction> list, String query)` vs `TransactionService.filterTransactions(List<Transaction> list, LocalDate from, LocalDate to)` vs `TransactionService.filterTransactions(List<Transaction> list, TransactionType type)`.
    - `CurrencyFormatter.format(BigDecimal amount)` vs `CurrencyFormatter.format(BigDecimal amount, CurrencyUnit currency)`.

### 1.4 Abstraction
* **Definition**: Hiding internal implementation complexities and exposing only essential functional interfaces to clients.
* **Implementation in PennyWise**:
  - **Abstract Base Classes**:
    - `Transaction` cannot be instantiated directly; only concrete `Income` or `Expense` records exist.
  - **Service Interfaces**:
    - `AuthService`, `IncomeService`, `ExpenseService`, `BudgetService`, `SavingsGoalService`, `TransactionService`, `ReportService`.
    - View controllers depend strictly on service interfaces rather than concrete implementations.
  - **Repository Interfaces**:
    - Data access contracts are defined in interfaces (`UserRepository`, `IncomeRepository`, `ExpenseRepository`, etc.), decoupling business logic from raw JDBC queries.

---

## 2. Advanced Java Language Concepts

### 2.1 Generics
* `public interface CrudRepository<T, ID>`: Provides generic CRUD method signatures (`save(T)`, `findById(ID)`, `findAll()`, `update(T)`, `deleteById(ID)`).
* `ValidationResult<T>`: Reusable generic container carrying validation status, errors, and target payload.

### 2.2 Collections Framework
* `List<Transaction>`, `Map<ExpenseCategory, BigDecimal>`, `Set<String>`.
* Used extensively for sorting transactions, grouping category totals, and filtering date windows.

### 2.3 Java Streams and Lambda Expressions
* Calculation of total income and expenses using functional map-reduce pipelines:
  ```java
  BigDecimal totalExpense = expenses.stream()
      .map(Expense::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  ```
* Category grouping:
  ```java
  Map<String, BigDecimal> categorySpending = expenses.stream()
      .collect(Collectors.groupingBy(
          e -> e.getCategory().getName(),
          Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
      ));
  ```

### 2.4 Strong Typing with Java Enums
* `TransactionType` (INCOME, EXPENSE)
* `IncomeSource` (SALARY, FREELANCING, BUSINESS, SCHOLARSHIP, INVESTMENTS, GIFTS, OTHER)
* `PaymentMethod` (CASH, CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, OTHER)
* `GoalStatus` (IN_PROGRESS, COMPLETED, CANCELLED)
* `NotificationType` (INFO, SUCCESS, WARNING, DANGER)
* `CurrencyUnit` (INR, USD, EUR, GBP)

### 2.5 Robust Exception Handling
* Custom unchecked exception hierarchy rooted at `PennyWiseException`.
* Try-with-resources blocks on all `Connection`, `PreparedStatement`, and `ResultSet` handles to guarantee deterministic resource deallocation and prevent JDBC socket leaks.

### 2.6 Multithreading & Asynchronous Tasks
* JavaFX Application Thread separation: Background database queries and analytics recalculations use `javafx.concurrent.Task` or background threads so the desktop GUI remains responsive at 60 FPS without UI freezing.

### 2.7 File Handling & I/O
* `CsvExporter`: Implements CSV formatting and file stream writing with proper character encoding (UTF-8) and RFC 4180 escaping.

