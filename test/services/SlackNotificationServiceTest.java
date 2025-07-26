package services;

import models.User;
import models.UserRole;
import org.junit.Test;
import play.mvc.Http;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SlackNotificationService
 */
public class SlackNotificationServiceTest {

    @Test
    public void testIsEnabledWithNoWebhookUrl() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Should not be enabled without webhook URL
        assertFalse(service.isEnabled());
    }

    @Test
    public void testNotifyUserRegistrationWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test user
        User user = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyUserRegistration(user, request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }

    @Test
    public void testNotifyUserLoginWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test user
        User user = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyUserLogin(user, request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }

    @Test
    public void testNotifyUserUpdateWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test users
        User currentUser = new User("admin", "admin@example.com", "password", UserRole.ADMIN);
        User targetUser = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyUserUpdate(currentUser, targetUser, "Test Action", request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }

    @Test
    public void testNotifyEquipmentOperationWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test user
        User user = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyEquipmentOperation(user, "Test Equipment", "Create", request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }

    @Test
    public void testNotifyEquipmentReservationWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test user
        User user = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyEquipmentReservation(user, "Test Equipment", "Reserve", request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }

    @Test
    public void testNotifyGenericWhenDisabled() {
        // Mock dependencies
        WSClient wsClient = mock(WSClient.class);
        ClassLoaderExecutionContext context = mock(ClassLoaderExecutionContext.class);
        Http.Request request = mock(Http.Request.class);
        
        // Create service (no webhook URL set)
        SlackNotificationService service = new SlackNotificationService(wsClient, context);
        
        // Create test user
        User user = new User("testuser", "test@example.com", "password");
        
        // Should return completed future when disabled
        var result = service.notifyGeneric("Test Action", user, request);
        assertTrue(((CompletableFuture<Void>) result).isDone());
    }
}