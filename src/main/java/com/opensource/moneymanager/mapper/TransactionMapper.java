package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionMapper {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMapper.class);

    public static TransactionDto toDto(Transaction t) {
        if (t == null) {
            logger.warn("Attempting to map null Transaction entity to DTO");
            return null;
        }

        logger.debug("Mapping Transaction entity to DTO: id={}", t.getId());
        TransactionDto d = new TransactionDto();
        d.setId(t.getId());
        d.setAmount(t.getAmount());
        d.setDescription(t.getDescription());
        d.setDateTime(t.getDateTime());
        d.setType(t.getType());

        // Map account relationships
        if (t.getAccount() != null) {
            d.setAccountId(t.getAccount().getId());
        }
        if (t.getSourceAccount() != null) {
            d.setSourceAccountId(t.getSourceAccount().getId());
        }
        if (t.getDestinationAccount() != null) {
            d.setDestinationAccountId(t.getDestinationAccount().getId());
        }

        logger.debug("Successfully mapped Transaction to DTO");
        return d;
    }

    public static Transaction toEntity(TransactionDto d) {
        if (d == null) {
            logger.warn("Attempting to map null TransactionDto to entity");
            return null;
        }

        logger.debug("Mapping TransactionDto to Transaction entity: type={}", d.getType());
        Transaction t = new Transaction();
        t.setId(d.getId());
        t.setAmount(d.getAmount());
        t.setDescription(d.getDescription());
        t.setDateTime(d.getDateTime());
        t.setType(d.getType());

        // Note: Account entities should be loaded from service/repository
        // The DTO only contains IDs; actual Account objects are set by service

        logger.debug("Successfully mapped TransactionDto to Transaction entity");
        return t;
    }
}
