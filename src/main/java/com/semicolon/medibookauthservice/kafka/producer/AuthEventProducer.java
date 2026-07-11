package com.semicolon.medibookauthservice.kafka.producer;

import com.semicolon.medibookauthservice.dto.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event){

        String USER_REGISTERED_TOPIC = "user.registered";
        kafkaTemplate.send(USER_REGISTERED_TOPIC, event.getUserId().toString(), event);
        log.info("Published UserRegisteredEvent for userId: {}", event.getUserId());

    }

}
