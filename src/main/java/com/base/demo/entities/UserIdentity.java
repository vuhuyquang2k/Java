package com.base.demo.entities;

import com.base.demo.constants.enums.identity.UserEmailStatus;
import com.base.demo.constants.enums.identity.UserProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_identities", uniqueConstraints = {
        @UniqueConstraint(name = "uq_one_identity_per_user", columnNames = { "user_id" }),
        @UniqueConstraint(name = "uq_provider_uid", columnNames = { "provider", "provider_user_id" })
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "provider", nullable = false)
    @NotNull
    private UserProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    @NotNull
    private String providerUserId;

    @Column(name = "email")
    private String email;

    @Column(name = "email_status")
    private UserEmailStatus emailStatus;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
