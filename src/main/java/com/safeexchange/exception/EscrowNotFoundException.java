package com.safeexchange.exception;

public class EscrowNotFoundException extends RuntimeException {
    public EscrowNotFoundException(String message) {
        super(message);
    }
}