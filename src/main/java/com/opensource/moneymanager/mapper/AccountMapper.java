package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountMapper {

    private static final Logger logger = LoggerFactory.getLogger(AccountMapper.class);

    public static AccountDto toDto(Account a) {
        if (a == null) {
            logger.warn("Attempting to map null Account entity to DTO");
            return null;
        }

        logger.debug("Mapping Account entity to DTO: id={}, name={}", a.getId(), a.getName());
        AccountDto d = new AccountDto();
        d.setId(a.getId());
        d.setName(a.getName());
        d.setType(a.getType());
        d.setBalance(a.getBalance());
        d.setAccountNumber(a.getAccountNumber());
        d.setProvider(a.getProvider());
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        d.setDescription(a.getDescription());
        d.setIsActive(a.getIsActive());

        logger.debug("Successfully mapped Account to DTO");
        return d;
    }

    public static Account toEntity(AccountDto d) {
        if (d == null) {
            logger.warn("Attempting to map null AccountDto to entity");
            return null;
        }

        logger.debug("Mapping AccountDto to Account entity: name={}, type={}", d.getName(), d.getType());
        Account a = new Account();
        a.setId(d.getId());
        a.setName(d.getName());
        a.setType(d.getType());
        a.setBalance(d.getBalance());
        a.setAccountNumber(d.getAccountNumber());
        a.setProvider(d.getProvider());
        a.setCreatedAt(d.getCreatedAt());
        a.setUpdatedAt(d.getUpdatedAt());
        a.setDescription(d.getDescription());
        a.setIsActive(d.getIsActive());

        logger.debug("Successfully mapped AccountDto to Account entity");
        return a;
    }
}

