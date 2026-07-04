package com.semicolon.medibookauthservice.dto.response;

import com.semicolon.medibookauthservice.enums.Role;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AuthResponse {

    private String token;
    private Role role;
    private UUID userId;
    private String refreshToken;
}
