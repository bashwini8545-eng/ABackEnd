package com.example.backend.service;




import com.example.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Publisher for "welcome email" events.
 *
 * RIGHT NOW: just logs the event (so the full Send → API → event flow works
 * end-to-end without needing a Kafka broker running).
 *
 * LATER (when Kafka is added):
 *   1. Add dependency to pom.xml:
 *        <dependency>
 *          <groupId>org.springframework.kafka</groupId>
 *          <artifactId>spring-kafka</artifactId>
 *        </dependency>
 *   2. Add to application.properties:
 *        spring.kafka.bootstrap-servers=localhost:9092
 *        app.kafka.topic.welcome-email=welcome-email
 *   3. Inject KafkaTemplate<String, Object> kafkaTemplate
 *      and replace the log line below with:
 *        kafkaTemplate.send(topic, String.valueOf(user.getId()), payload);
 *   4. A separate consumer service (in this app or a different microservice)
 *      will @KafkaListener on that topic and actually send the email
 *      (via SES, SendGrid, JavaMailSender, etc.).
 */
@Service
public class EmailEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmailEventPublisher.class);

    public void publishWelcomeEvent(User user) {
        // The payload that will eventually be the Kafka message value
        var payload = new WelcomeEmailEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Hi " + user.getName() + ", Welcome to Agility. Your email is " + user.getEmail() + "."
        );

        // TODO: replace with kafkaTemplate.send(...) once Kafka is wired up
        log.info("[welcome-email] would publish to Kafka -> {}", payload);
    }

    /** Plain DTO that will become the Kafka message value (auto-serialized to JSON). */
    public record WelcomeEmailEvent(Long userId, String name, String email, String message) {}
}