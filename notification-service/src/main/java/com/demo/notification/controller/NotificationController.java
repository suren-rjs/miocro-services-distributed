package com.demo.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    // Map to store active SSE connections per user
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestHeader("X-Auth-User") String userId) {
        // Create emitter with a long timeout (30 mins)
        SseEmitter emitter = new SseEmitter(1800_000L);
        
        emitters.put(userId, emitter);
        log.info("New SSE connection for user: {}", userId);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        // Send an initial heartbeat/success message
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected to notification stream"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    public static void sendToUser(String userId, Object message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
                log.info("Successfully pushed SSE notification to user: {}", userId);
            } catch (IOException e) {
                log.error("Error sending SSE to user {}: {}", userId, e.getMessage());
                emitters.remove(userId);
            }
        }
    }
}
