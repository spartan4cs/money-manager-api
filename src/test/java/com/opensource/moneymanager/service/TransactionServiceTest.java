package com.opensource.moneymanager.service;

import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    private Transaction sampleTransaction;

    @BeforeEach
    public void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setId(1L);
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setDescription("Test transaction");
        sampleTransaction.setDateTime(LocalDateTime.now());
        sampleTransaction.setType("INCOME");
    }

    @Test
    public void testSave() {
        when(repository.save(sampleTransaction)).thenReturn(sampleTransaction);

        Transaction saved = service.save(sampleTransaction);

        assertNotNull(saved);
        assertEquals("INCOME", saved.getType());
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
        verify(repository, times(1)).save(sampleTransaction);
    }

    @Test
    public void testFindAll() {
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setType("EXPENSE");
        transaction2.setAmount(new BigDecimal("50.00"));

        when(repository.findAll()).thenReturn(Arrays.asList(sampleTransaction, transaction2));

        List<Transaction> transactions = service.findAll();

        assertEquals(2, transactions.size());
        assertEquals("INCOME", transactions.get(0).getType());
        assertEquals("EXPENSE", transactions.get(1).getType());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        Optional<Transaction> found = service.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("INCOME", found.get().getType());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    public void testFindByIdNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Transaction> found = service.findById(999L);

        assertFalse(found.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    public void testDeleteById() {
        doNothing().when(repository).deleteById(1L);

        service.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}

