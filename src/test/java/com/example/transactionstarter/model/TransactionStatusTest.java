package com.example.transactionstarter.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionStatusTest {

    @Test
    @DisplayName("PENDING state should allow transition to PROCESSING, COMPLETED, FAILED, and CANCELLED")
    void pendingStateTransitions() {
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PROCESSING));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.COMPLETED));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.FAILED));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.CANCELLED));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PENDING));
    }

    @Test
    @DisplayName("PROCESSING state should allow transition to COMPLETED, FAILED, and CANCELLED but not PENDING")
    void processingStateTransitions() {
        assertTrue(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.COMPLETED));
        assertTrue(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.FAILED));
        assertTrue(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.CANCELLED));
        assertFalse(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.PENDING));
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = {"COMPLETED", "FAILED", "CANCELLED"})
    @DisplayName("Terminal states should disallow transitioning to any different state")
    void terminalStateTransitions(TransactionStatus terminalStatus) {
        for (TransactionStatus target : TransactionStatus.values()) {
            if (target != terminalStatus) {
                assertFalse(terminalStatus.canTransitionTo(target),
                        String.format("%s should not be able to transition to %s", terminalStatus, target));
            }
        }
    }
}
