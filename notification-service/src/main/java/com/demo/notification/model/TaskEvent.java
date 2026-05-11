package com.demo.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskEvent {
    private Long taskId;
    private String ownerId;
    private String action;
    private String title;
    private String status;
    private String timestamp;
}
