package com.semicolon.medibookauthservice.dto.response;

import com.semicolon.medibookauthservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private Role role;
    private UUID userId;
    private String refreshToken;


}
