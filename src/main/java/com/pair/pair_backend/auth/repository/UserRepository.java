package com.pair.pair_backend.auth.repository;

import com.pair.pair_backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByLoginId(String loginId);
}
