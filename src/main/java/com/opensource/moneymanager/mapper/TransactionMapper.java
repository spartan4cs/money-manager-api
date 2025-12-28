package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.model.Transaction;

public class TransactionMapper {
    public static TransactionDto toDto(Transaction t) {
        if (t == null) return null;
        TransactionDto d = new TransactionDto();
        d.setId(t.getId());
        d.setAmount(t.getAmount());
        d.setDescription(t.getDescription());
        d.setDateTime(t.getDateTime());
        d.setType(t.getType());
        return d;
    }

    public static Transaction toEntity(TransactionDto d) {
        if (d == null) return null;
        Transaction t = new Transaction();
        t.setId(d.getId());
        t.setAmount(d.getAmount());
        t.setDescription(d.getDescription());
        t.setDateTime(d.getDateTime());
        t.setType(d.getType());
        return t;
    }
}

