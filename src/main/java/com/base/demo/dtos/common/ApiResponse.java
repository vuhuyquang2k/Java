package com.base.demo.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String timestamp;
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(data)
                .build();
    }

    public static  <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .status(status.value())
                .message(message)
                .build();
    }
}
