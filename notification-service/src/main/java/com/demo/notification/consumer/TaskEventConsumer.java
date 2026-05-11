package com.demo.notification.consumer;

import com.demo.notification.model.TaskEvent;
import com.demo.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskEventConsumer {
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    public Consumer<TaskEvent> taskEvents() {
        return event -> {
            log.info("Kafka Event received: [ID: {}, Action: {}, User: {}]", 
                event.getTaskId(), event.getAction(), event.getOwnerId());
            
            // 1. Existing customized logic (e.g. Email)
            emailService.sendEmail("Notification for " + event.getOwnerId() + ": Task " + event.getTitle() + " was " + event.getAction());

            // 2. Broadcast via Redis to all Notification Service instances
            redisTemplate.convertAndSend("user-notifications", event);
        };
    }
}
