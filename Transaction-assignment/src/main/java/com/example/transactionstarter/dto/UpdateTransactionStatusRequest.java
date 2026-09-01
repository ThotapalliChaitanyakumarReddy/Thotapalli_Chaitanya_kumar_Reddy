package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTransactionStatusRequest {

    @NotNull(message = "Status must not be null")
    private TransactionStatus status;

    private String reason;

    public UpdateTransactionStatusRequest() {
    }

    public UpdateTransactionStatusRequest(TransactionStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
