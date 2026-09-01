package com.example.transactionstarter.model;

import java.util.Set;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    /**
     * Determines whether transitioning from this status to target status is valid.
     * Terminal states (COMPLETED, FAILED, CANCELLED) cannot transition to any other status.
     */
    public boolean canTransitionTo(TransactionStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case PENDING -> Set.of(PROCESSING, COMPLETED, FAILED, CANCELLED).contains(target);
            case PROCESSING -> Set.of(COMPLETED, FAILED, CANCELLED).contains(target);
            case COMPLETED, FAILED, CANCELLED -> false;
        };
    }
}
