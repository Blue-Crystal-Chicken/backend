package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException() {
        super("Email not found");
    }
}
