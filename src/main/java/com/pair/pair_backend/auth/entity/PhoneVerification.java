package com.pair.pair_backend.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "phone_verifications")
public class PhoneVerification {
    public enum Purpose {
        SIGN_UP, ACCOUNT_CHANGE
    }

    @Id
    @Column(nullable = false, unique = true, length = 64)
    private String verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User userId;

    @Column(nullable = false, length = 11)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false, length = 6)
    private LocalDateTime otpExpiresAt;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer attemptCount;

    @Column(length = 6)
    private LocalDateTime blockedUntil;

    @Column(length = 6)
    private LocalDateTime lastSentAt;

    @Column(unique = true, length = 64, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(length = 6)
    private LocalDateTime tokenExpiresAt;

    @Column(length = 6)
    private LocalDateTime consumedAt;

    @Column(nullable = false, length = 6)
    private LocalDateTime createdAt;


}
