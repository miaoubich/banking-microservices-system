package com.miaoubich.banking.exception;

public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 123456789L;

	public UserNotFoundException(String id) {
        super("User not found with ID: " + id);
    }
}