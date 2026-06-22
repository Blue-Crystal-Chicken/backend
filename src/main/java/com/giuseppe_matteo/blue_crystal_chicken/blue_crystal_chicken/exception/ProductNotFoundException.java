package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("Prodotto non trovato");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProductNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}