package com.opensource.moneymanager.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDtoTest {

    @Test
    public void testValidTransactionTypeIncome() {
        TransactionDto dto = new TransactionDto();
        dto.setType("INCOME");
        assertEquals("INCOME", dto.getType());
    }

    @Test
    public void testValidTransactionTypeExpense() {
        TransactionDto dto = new TransactionDto();
        dto.setType("EXPENSE");
        assertEquals("EXPENSE", dto.getType());
    }

    @Test
    public void testValidTransactionTypeTransfer() {
        TransactionDto dto = new TransactionDto();
        dto.setType("TRANSFER");
        assertEquals("TRANSFER", dto.getType());
    }

    @Test
    public void testInvalidTransactionType() {
        TransactionDto dto = new TransactionDto();
        assertThrows(IllegalArgumentException.class, () -> {
            dto.setType("INVALID");
        });
    }

    @Test
    public void testNullTypeIsAllowed() {
        TransactionDto dto = new TransactionDto();
        dto.setType(null);
        assertNull(dto.getType());
    }

    @Test
    public void testFullTransactionDto() {
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);
        dto.setAmount(new BigDecimal("100.50"));
        dto.setDescription("Salary payment");
        dto.setDateTime(LocalDateTime.now());
        dto.setType("INCOME");

        assertEquals(1L, dto.getId());
        assertEquals(new BigDecimal("100.50"), dto.getAmount());
        assertEquals("Salary payment", dto.getDescription());
        assertEquals("INCOME", dto.getType());
        assertNotNull(dto.getDateTime());
    }

    @Test
    public void testCaseInsensitivity() {
        TransactionDto dto = new TransactionDto();
        // The current implementation is case-sensitive, this will fail
        // If you want case-insensitive, update the regex to use (?i) flag
        assertThrows(IllegalArgumentException.class, () -> {
            dto.setType("income");  // lowercase should fail with current implementation
        });
    }
}

