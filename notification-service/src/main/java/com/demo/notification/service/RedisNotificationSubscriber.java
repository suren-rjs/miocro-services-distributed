package com.demo.notification.service;

import com.demo.notification.controller.NotificationController;
import com.demo.notification.model.TaskEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            TaskEvent event = objectMapper.readValue(message.getBody(), TaskEvent.class);
            log.info("Redis Broadcast received for user {}: {}", event.getOwnerId(), event.getAction());
            NotificationController.sendToUser(event.getOwnerId(), event);
        } catch (Exception e) {
            log.error("Failed to parse Redis message", e);
        }
    }
}
