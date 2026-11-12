package com.pair.pair_backend.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @Column(nullable = false, length = 64)
    private String refreshTokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AuthSession session;

    @Column(nullable = false, unique = true, length = 64, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime revokedAt;

    public static RefreshToken issue(
            AuthSession session,
            String tokenHash,
            LocalDateTime now,
            LocalDateTime expiresAt
    ) {
        RefreshToken token = new RefreshToken();
        token.refreshTokenId = "rft_" + UUID.randomUUID();
        token.session = session;
        token.tokenHash = tokenHash;
        token.issuedAt = now;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null
                && revokedAt == null
                && expiresAt.isAfter(now);
    }

    public void markUsed(LocalDateTime now) {
        if (usedAt == null) {
            usedAt = now;
        }
    }

    public void revoke(LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }
}