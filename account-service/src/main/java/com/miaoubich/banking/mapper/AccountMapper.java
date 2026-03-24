package com.miaoubich.banking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.miaoubich.banking.domain.Account;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;

@Mapper(componentModel = "spring")
public interface AccountMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "accountNumber", ignore = true)
	@Mapping(target = "balance", source = "initialBalance")
	@Mapping(target = "accountStatus", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	Account toAccount(CreateAccountRequest request);

	CreateAccountResponse toResponse(Account account);
}
