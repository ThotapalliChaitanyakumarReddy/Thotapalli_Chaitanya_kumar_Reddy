package com.example.transactionstarter.exception;

import com.example.transactionstarter.model.TransactionStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TransactionStatus currentStatus, TransactionStatus targetStatus) {
        super(String.format("Cannot transition transaction status from %s to %s", currentStatus, targetStatus));
    }
}
