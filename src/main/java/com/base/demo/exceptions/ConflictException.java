package com.base.demo.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}