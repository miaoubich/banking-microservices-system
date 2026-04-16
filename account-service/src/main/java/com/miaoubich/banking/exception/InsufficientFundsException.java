package com.miaoubich.banking.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    private static final long serialVersionUID = 123456789L;

    public InsufficientFundsException(BigDecimal requested, BigDecimal available) {
        super("Insufficient funds: requested " + requested + " but available balance is " + available);
    }
}
