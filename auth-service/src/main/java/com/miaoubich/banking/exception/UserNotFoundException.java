package com.miaoubich.banking.exception;

public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 123456789L;

	public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
}