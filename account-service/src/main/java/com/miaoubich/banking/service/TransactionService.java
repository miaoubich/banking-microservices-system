package com.miaoubich.banking.service;

import java.time.LocalDateTime;
import java.util.List;

import com.miaoubich.banking.dto.TransactionResponse;

public interface TransactionService {

    List<TransactionResponse> getTransactionsByAccountId(Long accountId);

    List<TransactionResponse> getTransactionsByAccountIdAndDateRange(Long accountId, LocalDateTime from, LocalDateTime to);
}
