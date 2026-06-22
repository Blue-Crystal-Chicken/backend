package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
