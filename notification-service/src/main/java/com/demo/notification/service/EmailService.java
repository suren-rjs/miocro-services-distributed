package com.demo.notification.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Retry(name = "emailService", fallbackMethod = "fallbackEmail")
    public void sendEmail(String message) {
        log.info("Attempting to send email: {}", message);
        // Simulate failure for demo purposes
        if (message.contains("Fail")) {
            throw new RuntimeException("External Email Provider Down!");
        }
        log.info("Email sent successfully!");
    }

    public void fallbackEmail(String message, Throwable t) {
        log.error("All retry attempts failed for message: {}. Reason: {}. Saving to fallback log.", message, t.getMessage());
    }
}
