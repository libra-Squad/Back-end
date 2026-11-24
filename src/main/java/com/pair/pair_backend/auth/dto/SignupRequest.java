package com.pair.pair_backend.auth.dto;

import com.pair.pair_backend.auth.entity.User.UserGender;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SignupRequest {
    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;
    private UserGender userGender;
    private String email;
    private String phone;


}
