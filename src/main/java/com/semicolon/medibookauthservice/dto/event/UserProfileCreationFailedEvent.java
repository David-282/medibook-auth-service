package com.semicolon.medibookauthservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileCreationFailedEvent {

    private UUID userId;
    private String errorMessage;
}
