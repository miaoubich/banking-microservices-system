package com.miaoubich.banking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.Transaction;
import com.miaoubich.banking.dto.TransactionResponse;
import com.miaoubich.banking.exception.AccountNotFoundException;
import com.miaoubich.banking.repository.AccountRepository;
import com.miaoubich.banking.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountIdAndDateRange(Long accountId, LocalDateTime from, LocalDateTime to) {
        accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(accountId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountNumber(), t.getType(), t.getAmount(), t.getBalanceAfter(), t.getCreatedAt());
    }
}
