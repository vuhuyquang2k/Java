package com.base.demo.dtos.deposit_request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateDepositRequest {

    @NotNull(message = "Amount không được null")
    @DecimalMin(value = "10000.00", message = "Số tiền tối thiểu là 10,000")
    @DecimalMax(value = "100000000.00", message = "Số tiền tối đa là 100,000,000")
    @Digits(integer = 15, fraction = 2, message = "Số tiền phải có tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal amount;

    @NotBlank(message = "Transaction code không được để trống")
    private String transactionCode;
}