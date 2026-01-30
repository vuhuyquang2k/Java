package com.base.demo.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException {
    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }
}