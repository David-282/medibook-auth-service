package com.semicolon.medibookauthservice.dto.event;

import com.semicolon.medibookauthservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserRegisteredEvent {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
}
