package com.semicolon.medibookauthservice.data.models;

import com.semicolon.medibookauthservice.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "auth_user")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String PasswordHash;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;

    private Instant refreshTokenExpiry;
}
