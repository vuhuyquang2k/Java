package com.base.demo.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler - Xử lý tất cả exception trong ứng dụng
 * Sử dụng @RestControllerAdvice để áp dụng cho tất cả REST controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý BaseException và các custom exceptions kế thừa từ nó
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("BaseException: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    /**
     * Xử lý validation errors từ @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .code(error.getCode())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .errorCode("VALIDATION_ERROR")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý constraint violation (validation ở method level)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.ValidationError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .errorCode("CONSTRAINT_VIOLATION")
                .message("Validation constraint violated")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi authentication (đăng nhập sai)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("BAD_CREDENTIALS");

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi authentication chung
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed: " + ex.getMessage(),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("AUTHENTICATION_FAILED");

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi authorization (không có quyền truy cập)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You don't have permission to access this resource",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("ACCESS_DENIED");

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Xử lý lỗi database constraint (duplicate key, foreign key, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String message = "Database constraint violation";
        String errorCode = "DATA_INTEGRITY_VIOLATION";

        // Phân tích lỗi cụ thể
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "Duplicate entry. This record already exists";
            errorCode = "DUPLICATE_ENTRY";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "Cannot perform this operation due to related data";
            errorCode = "FOREIGN_KEY_VIOLATION";
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                message,
                request.getRequestURI()
        );
        errorResponse.setErrorCode(errorCode);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Xử lý lỗi HTTP method không được hỗ trợ
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.error("Method not supported: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                String.format("Method '%s' is not supported for this endpoint", ex.getMethod()),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("METHOD_NOT_ALLOWED");

        Map<String, Object> details = new HashMap<>();
        details.put("supportedMethods", ex.getSupportedHttpMethods());
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Xử lý lỗi media type không được hỗ trợ
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.error("Media type not supported: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                String.format("Media type '%s' is not supported", ex.getContentType()),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("UNSUPPORTED_MEDIA_TYPE");

        Map<String, Object> details = new HashMap<>();
        details.put("supportedMediaTypes", ex.getSupportedMediaTypes());
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Xử lý lỗi JSON parse
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("Message not readable: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Malformed JSON request or invalid data format",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("MALFORMED_JSON");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi thiếu request parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error("Missing parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("MISSING_PARAMETER");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi type mismatch (ví dụ: truyền string vào parameter integer)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("Type mismatch: {}", ex.getMessage());

        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );
        errorResponse.setErrorCode("TYPE_MISMATCH");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi file upload quá lớn
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.error("File size exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Payload Too Large",
                "File size exceeds the maximum allowed limit",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("FILE_SIZE_EXCEEDED");

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Xử lý lỗi 404 - không tìm thấy endpoint
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("No handler found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                String.format("Endpoint '%s' not found", ex.getRequestURL()),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("ENDPOINT_NOT_FOUND");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        errorResponse.setErrorCode("ILLEGAL_ARGUMENT");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(
            NullPointerException ex, HttpServletRequest request) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("NULL_POINTER");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Xử lý tất cả các exception chưa được xử lý cụ thể
     * Đây là handler cuối cùng để catch tất cả lỗi
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support if the problem persists",
                request.getRequestURI()
        );
        errorResponse.setErrorCode("INTERNAL_SERVER_ERROR");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}