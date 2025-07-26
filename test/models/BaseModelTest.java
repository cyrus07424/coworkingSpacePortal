package models;

import org.junit.Test;
import play.test.WithApplication;

import java.time.Instant;

import static org.junit.Assert.*;

public class BaseModelTest extends WithApplication {

    @Test
    public void testTimestampFields() {
        // Create a new user to test timestamp functionality
        User user = new User("test", "test@example.com", "password123");
        
        // Check that timestamps are not set initially
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        
        // Manually trigger onCreate to set timestamps before save
        user.onCreate();
        
        // Check that timestamps are now set
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        
        // Save the user
        user.save();
        
        // Refresh from DB to get the actual values
        user.refresh();
        
        // Check that timestamps are still set
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
        
        // Update the user - this should trigger the PreUpdate method
        Instant originalCreated = user.getCreatedAt();
        Instant originalUpdated = user.getUpdatedAt();
        
        try {
            Thread.sleep(10); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        user.setUsername("updated_test");
        user.onUpdate();
        user.save();
        user.refresh();
        
        // Check that created_at hasn't changed but updated_at has
        assertEquals(originalCreated, user.getCreatedAt());
        assertTrue(user.getUpdatedAt().isAfter(originalUpdated));
    }
}