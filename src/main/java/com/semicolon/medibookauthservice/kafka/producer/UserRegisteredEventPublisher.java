package com.semicolon.medibookauthservice.kafka.producer;

import com.semicolon.medibookauthservice.dto.event.UserRegisteredApplicationEvent;
import com.semicolon.medibookauthservice.dto.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventPublisher {

    private final AuthEventProducer authEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredApplicationEvent applicationEvent) {
        authEventProducer.publishUserRegisteredEvent(applicationEvent.getEvent());
    }
}
