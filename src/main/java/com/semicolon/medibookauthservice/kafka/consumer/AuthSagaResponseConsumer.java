package com.semicolon.medibookauthservice.kafka.consumer;

import com.semicolon.medibookauthservice.data.repository.AuthUserRepository;
import com.semicolon.medibookauthservice.dto.event.UserProfileCreatedEvent;
import com.semicolon.medibookauthservice.dto.event.UserRegistrationFailedEvent;
import com.semicolon.medibookauthservice.enums.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@AllArgsConstructor
public class AuthSagaResponseConsumer {

    private final AuthUserRepository authUserRepository;

    @KafkaListener(topics = "user.profile.created", groupId = "auth-saga-group")
    @Transactional
    public void consumeUserProfileCreated(UserProfileCreatedEvent event) {
        log.info("Saga Success: Activating auth account for user: {}", event.getUserId());

        authUserRepository.findById(event.getUserId())
                .ifPresent(user -> {
                    user.setAccountStatus(AccountStatus.ACTIVE);
                    authUserRepository.saveAndFlush(user);
                });
    }

    @KafkaListener(topics = "user.registration.failed", groupId = "auth-saga-group")
    @Transactional
    public void handleRegistrationFailed(UserRegistrationFailedEvent event) {
        log.error("Saga Failure: User service failed for ID: {}. Triggering compensating transaction.", event.getUserId());

         authUserRepository.findById(event.getUserId())
            .ifPresent(user -> user.setAccountStatus(AccountStatus.REGISTRATION_FAILED));
    }


}
