package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}
