package com.opensource.moneymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.model.Transaction;
import com.opensource.moneymanager.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction sampleTransaction;
    private TransactionDto sampleDto;

    @BeforeEach
    public void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setId(1L);
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setDescription("Test transaction");
        sampleTransaction.setDateTime(LocalDateTime.now());
        sampleTransaction.setType("INCOME");

        sampleDto = new TransactionDto();
        sampleDto.setId(1L);
        sampleDto.setAmount(new BigDecimal("100.00"));
        sampleDto.setDescription("Test transaction");
        sampleDto.setDateTime(LocalDateTime.now());
        sampleDto.setType("INCOME");
    }

    @Test
    public void testGetAllTransactions() throws Exception {
        when(service.findAll()).thenReturn(Arrays.asList(sampleTransaction));

        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("INCOME"))
            .andExpect(jsonPath("$[0].amount").value(100.00));

        verify(service, times(1)).findAll();
    }

    @Test
    public void testGetTransactionById() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        mockMvc.perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(jsonPath("$.description").value("Test transaction"));

        verify(service, times(1)).findById(1L);
    }

    @Test
    public void testGetTransactionByIdNotFound() throws Exception {
        when(service.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/999"))
            .andExpect(status().isNotFound());

        verify(service, times(1)).findById(999L);
    }

    @Test
    public void testCreateTransaction() throws Exception {
        when(service.save(any(Transaction.class))).thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(header().exists("Location"));

        verify(service, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        doNothing().when(service).deleteById(1L);

        mockMvc.perform(delete("/api/transactions/1"))
            .andExpect(status().isNoContent());

        verify(service, times(1)).deleteById(1L);
    }
}

