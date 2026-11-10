package com.pair.pair_backend.auth.dto;

import com.pair.pair_backend.auth.entity.User;
import com.pair.pair_backend.auth.entity.User.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private DataDto data;

    @Getter
    @Builder
    public static class DataDto {
        private String accessToken;
        private String tokenType;
        private Integer expiresIn;
        private UserDto user;
    }

    @Getter
    @Builder
    public static class UserDto {
        private String userId;
        private UserRole role;
        private String companyId;
        private String name;
    }

    public static LoginResponse from(
            String accessToken,
            User user,
            String companyId
    ) {
        UserDto userDto = UserDto.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .companyId(companyId)
                .name(user.getName())
                .build();

        DataDto data = DataDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .user(userDto)
                .build();

        return LoginResponse.builder()
                .data(data)
                .build();
    }

}
