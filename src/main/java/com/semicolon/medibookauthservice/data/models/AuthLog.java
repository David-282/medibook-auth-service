package com.semicolon.medibookauthservice.data.models;

import com.semicolon.medibookauthservice.enums.AuthLogAction;
import com.semicolon.medibookauthservice.enums.AuthStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "auth_logs")
public class AuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "auth_user_id", nullable = false)
    private AuthUser authUser;

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private AuthStatus status;


    @Enumerated(EnumType.STRING)
    private AuthLogAction action;

    @CreationTimestamp
    private Instant createdAt;
}
