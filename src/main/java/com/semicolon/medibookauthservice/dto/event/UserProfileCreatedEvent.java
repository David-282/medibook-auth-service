package com.semicolon.medibookauthservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserProfileCreatedEvent {

    private UUID userId;
    private Instant createdAt = Instant.now();
}
