package com.example.backend.service;



import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer — where business logic lives.
 *
 * Keeping logic here (instead of in the Controller) makes it:
 *   - testable in isolation
 *   - reusable across different controllers / scheduled jobs
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailEventPublisher emailEventPublisher;

    public UserService(UserRepository userRepository, EmailEventPublisher emailEventPublisher) {
        this.userRepository = userRepository;
        this.emailEventPublisher = emailEventPublisher;
    }

    public User create(User user) {
        // Business rule: don't allow duplicate emails
        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        });
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + id));
    }

    /**
     * Triggered by the "Send" button on the welcome page.
     * Publishes a welcome-email event that Kafka (later) will deliver.
     */
    public void sendWelcomeEmail(Long id) {
        User user = findById(id);
        emailEventPublisher.publishWelcomeEvent(user);
    }
}