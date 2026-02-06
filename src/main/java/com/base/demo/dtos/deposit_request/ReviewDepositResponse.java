package com.base.demo.dtos.deposit_request;

import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewDepositResponse {

    private Long id;
    private DepositRequestStatus status;
    @Digits(integer = 15, fraction = 2, message = "Số tiền phải có tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal amount;
    private Long walletId;
    @Digits(integer = 15, fraction = 2, message = "Số tiền phải có tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal balanceAfter;
    private LocalDateTime processedAt;
    private Long processedBy;
}
