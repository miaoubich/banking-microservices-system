package com.miaoubich.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.banking.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
