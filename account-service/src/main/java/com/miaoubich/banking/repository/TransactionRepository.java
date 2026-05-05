package com.miaoubich.banking.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.banking.domain.Transaction;
import com.miaoubich.banking.domain.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long accountId, LocalDateTime from, LocalDateTime to);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    boolean existsByAccountIdAndAmountAndTypeAndCreatedAtAfter(Long accountId, BigDecimal amount, TransactionType type, LocalDateTime after);
}
