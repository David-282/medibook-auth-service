package com.semicolon.medibookauthservice.data.models;

import com.semicolon.medibookauthservice.enums.AccountStatus;
import com.semicolon.medibookauthservice.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "auth_users", indexes = {

        @Index(name = "idx_auth_user_email", columnList = "email", unique = true)
})
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING_PROFILE;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;

    private Instant refreshTokenExpiry;
}
