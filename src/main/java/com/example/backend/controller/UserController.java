package com.example.backend.controller;


import com.example.backend.model.User;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller — exposes HTTP endpoints under /api/users.
 *
 * Endpoints:
 *   POST /api/users                      → create a user
 *   GET  /api/users                      → list all users
 *   GET  /api/users/{id}                 → fetch one user (welcome page)
 *   POST /api/users/{id}/send-welcome    → publish welcome event (Kafka later)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User saved = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getOne(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * Called by the Send button on the welcome page.
     * Returns immediately; the actual email delivery will be done by a
     * Kafka consumer (added later).
     */
    @PostMapping("/{id}/send-welcome")
    public ResponseEntity<Map<String, Object>> sendWelcome(@PathVariable Long id) {
        userService.sendWelcomeEmail(id);
        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "userId", id,
                "message", "Welcome email queued for delivery."
        ));
    }

    // ===== Simple error handling =====

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}


