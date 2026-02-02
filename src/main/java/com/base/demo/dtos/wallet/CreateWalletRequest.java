package com.base.demo.dtos.wallet;

import com.base.demo.constants.enums.wallet.WalletStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class CreateWalletRequest {

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @Builder.Default
    private BigDecimal balance = new BigDecimal("0.00");

    @Builder.Default
    private BigDecimal pendingBalance = new BigDecimal("0.00");

    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Builder.Default
    private Integer version = 1;
}
