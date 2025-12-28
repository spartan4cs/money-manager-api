package com.opensource.moneymanager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionTest {

    @Test
    public void testTransactionCreationWithValidType() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction(
            new BigDecimal("500.00"),
            "Office supplies",
            now,
            "EXPENSE"
        );

        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
        assertEquals("Office supplies", transaction.getDescription());
        assertEquals(now, transaction.getDateTime());
        assertEquals("EXPENSE", transaction.getType());
    }

    @Test
    public void testTransactionCreationWithInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction(
                new BigDecimal("500.00"),
                "Office supplies",
                LocalDateTime.now(),
                "INVALID_TYPE"
            );
        });
    }

    @Test
    public void testSetTypeValidation() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));

        transaction.setType("INCOME");
        assertEquals("INCOME", transaction.getType());

        transaction.setType("EXPENSE");
        assertEquals("EXPENSE", transaction.getType());

        transaction.setType("TRANSFER");
        assertEquals("TRANSFER", transaction.getType());
    }

    @Test
    public void testSetTypeValidationFails() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> {
            transaction.setType("UNKNOWN");
        });
    }

    @Test
    public void testTransactionAllFields() {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(new BigDecimal("250.75"));
        t.setDescription("Bank transfer");
        LocalDateTime dt = LocalDateTime.now();
        t.setDateTime(dt);
        t.setType("TRANSFER");

        assertEquals(1L, t.getId());
        assertEquals(new BigDecimal("250.75"), t.getAmount());
        assertEquals("Bank transfer", t.getDescription());
        assertEquals(dt, t.getDateTime());
        assertEquals("TRANSFER", t.getType());
    }
}

