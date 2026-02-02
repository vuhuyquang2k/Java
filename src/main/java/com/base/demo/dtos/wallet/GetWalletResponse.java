package com.base.demo.dtos.wallet;

import com.base.demo.constants.enums.wallet.WalletStatus;
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
public class GetWalletResponse {
    private Integer userId;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private WalletStatus status;
}
