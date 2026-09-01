package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateTransactionStatusRequest;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private CreateTransactionRequest createRequest;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        createRequest = new CreateTransactionRequest(
                "TXN-1001",
                "CUST-001",
                new BigDecimal("150.00"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        sampleTransaction = new Transaction(
                "TXN-1001",
                "CUST-001",
                new BigDecimal("150.00"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );
    }

    @Test
    @DisplayName("createTransaction should create and save transaction successfully")
    void createTransaction_success() {
        when(transactionRepository.existsByTransactionId("TXN-1001")).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = transactionService.createTransaction(createRequest);

        assertNotNull(response);
        assertEquals("TXN-1001", response.getTransactionId());
        assertEquals("CUST-001", response.getCustomerId());
        assertEquals(TransactionStatus.PENDING, response.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction should throw DuplicateTransactionException if ID already exists")
    void createTransaction_duplicateId() {
        when(transactionRepository.existsByTransactionId("TXN-1001")).thenReturn(true);

        assertThrows(DuplicateTransactionException.class, () -> transactionService.createTransaction(createRequest));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction auto-generates ID when not provided")
    void createTransaction_autoGenerateId() {
        createRequest.setTransactionId(null);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(createRequest);

        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));
    }

    @Test
    @DisplayName("getTransactionById returns transaction response when found")
    void getTransactionById_success() {
        when(transactionRepository.findById("TXN-1001")).thenReturn(Optional.of(sampleTransaction));

        TransactionResponse response = transactionService.getTransactionById("TXN-1001");

        assertEquals("TXN-1001", response.getTransactionId());
        assertEquals("CUST-001", response.getCustomerId());
    }

    @Test
    @DisplayName("getTransactionById throws TransactionNotFoundException when ID does not exist")
    void getTransactionById_notFound() {
        when(transactionRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById("UNKNOWN"));
    }

    @Test
    @DisplayName("updateTransactionStatus updates status successfully for valid transition")
    void updateTransactionStatus_success() {
        when(transactionRepository.findById("TXN-1001")).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTransactionStatusRequest updateRequest = new UpdateTransactionStatusRequest(TransactionStatus.COMPLETED, "Payment settled");
        TransactionResponse response = transactionService.updateTransactionStatus("TXN-1001", updateRequest);

        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
    }

    @Test
    @DisplayName("updateTransactionStatus throws InvalidStatusTransitionException for illegal transition from COMPLETED")
    void updateTransactionStatus_invalidTransition() {
        sampleTransaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById("TXN-1001")).thenReturn(Optional.of(sampleTransaction));

        UpdateTransactionStatusRequest updateRequest = new UpdateTransactionStatusRequest(TransactionStatus.PENDING, "Reopening");

        assertThrows(InvalidStatusTransitionException.class, () -> transactionService.updateTransactionStatus("TXN-1001", updateRequest));
    }

    @Test
    @DisplayName("getTransactionsByCustomer returns transactions list for customer")
    void getTransactionsByCustomer_success() {
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc("CUST-001")).thenReturn(List.of(sampleTransaction));

        List<TransactionResponse> list = transactionService.getTransactionsByCustomer("CUST-001");

        assertEquals(1, list.size());
        assertEquals("CUST-001", list.get(0).getCustomerId());
    }
}
