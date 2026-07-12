package com.semicolon.medibookauthservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredApplicationEvent {

    private final UserRegisteredEvent event;
}
