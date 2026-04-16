package com.miaoubich.banking.exception;

public class AccountNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 123456789L;

    public AccountNotFoundException(Long accountId) {
        super("Account not found with ID: " + accountId);
    }
}
