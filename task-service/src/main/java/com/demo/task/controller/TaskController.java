package com.demo.task.controller;

import com.demo.task.model.Task;
import com.demo.task.model.TaskEvent;
import com.demo.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository repository;
    private final StreamBridge streamBridge;

    @PostMapping
    public Task createTask(@RequestBody Task task, @RequestHeader("X-Auth-User") String user) {
        task.setOwnerId(user);
        Task saved = repository.save(task);
        sendEvent(saved, "CREATED");
        return saved;
    }

    @GetMapping("/{id}")
    @Cacheable(value = "tasks", key = "#id")
    public Task getTask(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PutMapping("/{id}/complete")
    @CacheEvict(value = "tasks", key = "#id")
    public Task completeTask(@PathVariable Long id) {
        Task task = repository.findById(id).orElseThrow();
        task.setStatus("COMPLETED");
        Task saved = repository.save(task);
        sendEvent(saved, "COMPLETED");
        return saved;
    }

    @GetMapping
    public List<Task> getAllTasks(@RequestHeader("X-Auth-User") String user) {
        // Return only tasks owned by the user
        return repository.findAll().stream()
                .filter(t -> user.equals(t.getOwnerId()))
                .toList();
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "tasks", key = "#id")
    public Task updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Task task = repository.findById(id).orElseThrow();
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        Task updated = repository.save(task);
        sendEvent(updated, "UPDATED");
        return updated;
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(@PathVariable Long id) {
        Task task = repository.findById(id).orElseThrow();
        repository.delete(task);
        sendEvent(task, "DELETED");
    }

    private void sendEvent(Task task, String action) {
        TaskEvent event = TaskEvent.builder()
                .taskId(task.getId())
                .ownerId(task.getOwnerId())
                .action(action)
                .title(task.getTitle())
                .status(task.getStatus())
                .timestamp(LocalDateTime.now().toString())
                .build();

        // Use ownerId as the partition key to ensure all events for a user stay ordered
        streamBridge.send("taskEvents-out-0", 
            MessageBuilder.withPayload(event)
                .setHeader("partitionKey", task.getOwnerId())
                .build());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String handleNotFound(java.util.NoSuchElementException e) {
        return "Task not found";
    }
}

