package com.base.demo.entities;

import com.base.demo.constants.enums.wallet_transaction.WalletTransactionDirection;
import com.base.demo.constants.enums.wallet_transaction.WalletTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
@EntityListeners(AuditingEntityListener.class)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    @NotNull
    private Long walletId;

    @Column(name = "transaction_type", nullable = false)
    @NotNull
    private WalletTransactionType transactionType;

    @Column(name = "direction", nullable = false)
    @NotNull
    private WalletTransactionDirection direction;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal balanceAfter;

    @Column(name = "pending_before", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal pendingBefore;

    @Column(name = "pending_after", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal pendingAfter;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
