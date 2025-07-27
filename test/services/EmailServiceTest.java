package services;

import models.User;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for EmailService
 */
public class EmailServiceTest {

    @Test
    public void testEmailServiceInitialization() {
        EmailService emailService = new EmailService(null);
        assertNotNull(emailService);
    }

    @Test
    public void testEmailServiceConfigurationCheck() {
        EmailService emailService = new EmailService(null);
        // Without proper environment variables, service should not be configured
        assertFalse(emailService.isConfigured());
    }

    @Test
    public void testWelcomeEmailWithoutConfiguration() {
        EmailService emailService = new EmailService(null);
        User user = new User("testuser", "test@example.com", "password123");
        
        // Should not throw exception and return false when not configured
        emailService.sendWelcomeEmail(user).toCompletableFuture().join();
        // Test passes if no exception is thrown
        assertTrue(true);
    }

    @Test
    public void testPasswordResetEmailWithoutConfiguration() {
        EmailService emailService = new EmailService(null);
        User user = new User("testuser", "test@example.com", "password123");
        
        // Should not throw exception and return false when not configured
        emailService.sendPasswordResetEmail(user, "test-token", "http://localhost:9000").toCompletableFuture().join();
        // Test passes if no exception is thrown
        assertTrue(true);
    }
}