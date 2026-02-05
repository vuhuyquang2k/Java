package com.base.demo.entities;

import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_requests")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DepositRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull
    private Long userId;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "transaction_code", nullable = false)
    @NotBlank
    private String transactionCode;

    @Column(name = "transfer_reference")
    private String transferReference;

    @Column(name = "status", nullable = false)
    private DepositRequestStatus status;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
