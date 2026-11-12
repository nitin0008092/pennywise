package com.pennywise.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic container capturing validation status and error messages.
 * Demonstrates Generics and Immutable collection exposure.
 *
 * @param <T> the type of entity validated
 */
public class ValidationResult<T> {
    private final boolean valid;
    private final List<String> errors;
    private final T target;

    private ValidationResult(boolean valid, List<String> errors, T target) {
        this.valid = valid;
        this.errors = errors != null ? Collections.unmodifiableList(new ArrayList<>(errors)) : Collections.emptyList();
        this.target = target;
    }

    public static <T> ValidationResult<T> success(T target) {
        return new ValidationResult<>(true, Collections.emptyList(), target);
    }

    public static <T> ValidationResult<T> failure(List<String> errors, T target) {
        return new ValidationResult<>(false, errors, target);
    }

    public static <T> ValidationResult<T> failure(String error, T target) {
        return new ValidationResult<>(false, List.of(error), target);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getFirstError() {
        return errors.isEmpty() ? "" : errors.getFirst();
    }

    public String getCombinedErrors() {
        return String.join("\n", errors);
    }

    public T getTarget() {
        return target;
    }
}

