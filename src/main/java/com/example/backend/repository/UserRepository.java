package com.example.backend.repository;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository.
 *
 * Extending JpaRepository<User, Long> gives us, for free:
 *   - save(user)
 *   - findAll()
 *   - findById(id)
 *   - deleteById(id)
 *   - count(), existsById(), etc.
 *
 * No implementation class needed — Spring generates one at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom finder — Spring derives the SQL from the method name
    Optional<User> findByEmail(String email);
}