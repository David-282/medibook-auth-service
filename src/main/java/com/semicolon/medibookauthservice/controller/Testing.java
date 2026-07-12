package com.semicolon.medibookauthservice.controller;


import com.semicolon.medibookauthservice.dto.event.UserProfileCreationFailedEvent;
import com.semicolon.medibookauthservice.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/testing")
@RequiredArgsConstructor
public class Testing {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/simulate-failure/{userId}")
    public ResponseEntity<String> simulateFailure(@PathVariable UUID userId) {

        UserProfileCreationFailedEvent event = new UserProfileCreationFailedEvent(userId, "Sad path works well");

        kafkaTemplate.send("user.registration.failed", event);

        return ResponseEntity.ok("Failure event dispatched for user: " + userId);

    }


}
