package com.miaoubich.banking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.banking.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long accountId, LocalDateTime from, LocalDateTime to);
}
