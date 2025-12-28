package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountStructMapper {
    AccountDto toDto(Account account);

    // Ignore timestamps, relationships and isActive when mapping from DTO to entity so constructors/PrePersist handle them
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "outgoingTransfers", ignore = true)
    @Mapping(target = "incomingTransfers", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Account toEntity(AccountDto dto);
}
