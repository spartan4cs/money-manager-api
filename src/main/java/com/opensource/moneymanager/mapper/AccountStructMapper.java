package com.opensource.moneymanager.mapper;

import com.opensource.moneymanager.dto.AccountDto;
import com.opensource.moneymanager.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountStructMapper {
    AccountDto toDto(Account account);
    Account toEntity(AccountDto dto);
}
