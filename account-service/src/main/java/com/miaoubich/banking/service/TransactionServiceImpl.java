package com.miaoubich.banking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.Transaction;
import com.miaoubich.banking.dto.TransactionResponse;
import com.miaoubich.banking.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        accountService.findAccountByAccountId(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountIdAndDateRange(Long accountId, LocalDateTime from, LocalDateTime to) {
        accountService.findAccountByAccountId(accountId);
        return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(accountId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountNumber(), t.getType(), t.getAmount(), t.getBalanceAfter(), t.getCreatedAt());
    }
}
