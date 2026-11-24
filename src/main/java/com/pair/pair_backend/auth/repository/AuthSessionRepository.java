package com.pair.pair_backend.auth.repository;

import com.pair.pair_backend.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
}