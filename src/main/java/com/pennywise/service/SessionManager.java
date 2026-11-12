package com.pennywise.service;

import com.pennywise.model.User;
import com.pennywise.util.CurrencyFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe singleton managing the authenticated user session.
 */
public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final AtomicReference<User> currentUser = new AtomicReference<>(null);

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setCurrentUser(User user) {
        this.currentUser.set(user);
        if (user != null && user.getCurrency() != null) {
            CurrencyFormatter.setDefaultCurrency(user.getCurrency());
        }
    }

    public User getCurrentUser() {
        return currentUser.get();
    }

    public Long getCurrentUserId() {
        User u = currentUser.get();
        return u != null ? u.getId() : null;
    }

    public boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    public void clearSession() {
        currentUser.set(null);
    }
}

