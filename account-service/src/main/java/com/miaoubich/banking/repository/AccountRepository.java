package com.miaoubich.banking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.banking.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findByClientId(String clientId);
	Optional<Account> findByAccountNumber(String accountNumber);
}
