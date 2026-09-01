package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateTransactionStatusRequest;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/transactions - create transaction successfully")
    void createTransaction_success() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-2001",
                "CUST-002",
                new BigDecimal("99.99"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-2001"))
                .andExpect(jsonPath("$.customerId").value("CUST-002"))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/transactions - returns 400 Bad Request on invalid fields")
    void createTransaction_validationFailure() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "",
                "", // Blank customer ID
                new BigDecimal("-50.00"), // Negative amount
                "INVALID_CURRENCY", // Invalid currency pattern
                null, // Missing type
                null
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerId").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.currency").exists())
                .andExpect(jsonPath("$.fieldErrors.type").exists());
    }

    @Test
    @DisplayName("GET /api/transactions/{id} - get transaction by ID")
    void getTransactionById_success() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-2002",
                "CUST-002",
                new BigDecimal("250.00"),
                "EUR",
                TransactionType.TRANSFER,
                TransactionStatus.PENDING
        );

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/transactions/TXN-2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-2002"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} - returns 404 when transaction not found")
    void getTransactionById_notFound() throws Exception {
        mockMvc.perform(get("/api/transactions/NON_EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("NON_EXISTENT")));
    }

    @Test
    @DisplayName("PATCH /api/transactions/{id}/status - update status and verify 400 on terminal state violation")
    void updateTransactionStatus_flow() throws Exception {
        // 1. Create transaction PENDING
        CreateTransactionRequest createReq = new CreateTransactionRequest(
                "TXN-2003",
                "CUST-003",
                new BigDecimal("500.00"),
                "USD",
                TransactionType.DEPOSIT,
                TransactionStatus.PENDING
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // 2. Update status to COMPLETED
        UpdateTransactionStatusRequest updateReq = new UpdateTransactionStatusRequest(TransactionStatus.COMPLETED, "Success");

        mockMvc.perform(patch("/api/transactions/TXN-2003/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 3. Attempt illegal status update from COMPLETED -> PENDING
        UpdateTransactionStatusRequest invalidReq = new UpdateTransactionStatusRequest(TransactionStatus.PENDING, "Revert");

        mockMvc.perform(patch("/api/transactions/TXN-2003/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot transition transaction status from COMPLETED to PENDING")));
    }

    @Test
    @DisplayName("GET /api/customers/{customerId}/transactions - list transactions for customer")
    void getCustomerTransactions_success() throws Exception {
        // Create 2 transactions for CUST-005
        CreateTransactionRequest req1 = new CreateTransactionRequest("TXN-5001", "CUST-005", new BigDecimal("10.00"), "USD", TransactionType.PAYMENT, TransactionStatus.PENDING);
        CreateTransactionRequest req2 = new CreateTransactionRequest("TXN-5002", "CUST-005", new BigDecimal("20.00"), "USD", TransactionType.REFUND, TransactionStatus.COMPLETED);

        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req1)));
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req2)));

        mockMvc.perform(get("/api/customers/CUST-005/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
