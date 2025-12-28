package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.TransactionDto;
import com.opensource.moneymanager.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionStructMapper {

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "sourceAccount.id", target = "sourceAccountId")
    @Mapping(source = "destinationAccount.id", target = "destinationAccountId")
    TransactionDto toDto(Transaction transaction);

    // Ignore account references when mapping from DTO to entity; service layer should set them
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "sourceAccount", ignore = true)
    @Mapping(target = "destinationAccount", ignore = true)
    Transaction toEntity(TransactionDto dto);
}
