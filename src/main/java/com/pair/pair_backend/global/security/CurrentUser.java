package com.pair.pair_backend.global.security;

import com.pair.pair_backend.auth.entity.User;

public record CurrentUser(String userId, User.UserRole role, String companyId) {
}
