package com.pair.pair_backend.auth.repository;

import com.pair.pair_backend.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select token
        from RefreshToken token
        join fetch token.session
        where token.tokenHash = :tokenHash
    """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
        update RefreshToken token
        set token.revokedAt = :now
        where token.session.sessionId = :sessionId
          and token.usedAt is null
          and token.revokedAt is null
    """)
    int revokeUnusedForSession(
            @Param("sessionId") String sessionId,
            @Param("now") LocalDateTime now
    );
}