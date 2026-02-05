package com.base.demo.dtos.deposit_request;

import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class CreateDepositResponse {
    private Long id;
    private BigDecimal amount;
    private String transactionCode;
    private DepositRequestStatus status;
    private String message;
}
