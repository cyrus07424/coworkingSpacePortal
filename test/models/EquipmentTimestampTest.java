package models;

import org.junit.Test;
import play.test.WithApplication;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class EquipmentTimestampTest extends WithApplication {

    @Test
    public void testEquipmentTimestamps() {
        // Create and save a new equipment
        Equipment equipment = new Equipment("Test Equipment", new BigDecimal("100.50"), "Test Description", EquipmentCategory.DEVELOPMENT_BOARD);
        equipment.save();
        
        // Check that timestamps are set
        assertNotNull(equipment.getCreatedAt());
        assertNotNull(equipment.getUpdatedAt());
        assertEquals(equipment.getCreatedAt(), equipment.getUpdatedAt());
        
        // Update equipment
        equipment.setName("Updated Equipment");
        equipment.onUpdate();
        equipment.save();
        equipment.refresh();
        
        // Check that updated_at has changed
        assertTrue(equipment.getUpdatedAt().isAfter(equipment.getCreatedAt()));
    }

    @Test
    public void testEquipmentReservationTimestamps() {
        // Create a user and equipment first
        User user = new User("test", "test@example.com", "password123");
        user.save();
        
        Equipment equipment = new Equipment("Test Equipment", new BigDecimal("100.50"), "Test Description", EquipmentCategory.DEVELOPMENT_BOARD);
        equipment.save();
        
        // Create a reservation
        EquipmentReservation reservation = new EquipmentReservation(equipment, user, LocalDate.now());
        reservation.save();
        
        // Check that timestamps are set
        assertNotNull(reservation.getCreatedAt());
        assertNotNull(reservation.getUpdatedAt());
        assertEquals(reservation.getCreatedAt(), reservation.getUpdatedAt());
        
        // Update reservation
        reservation.setStatus(EquipmentReservation.ReservationStatus.CANCELLED);
        reservation.onUpdate();
        reservation.save();
        reservation.refresh();
        
        // Check that updated_at has changed
        assertTrue(reservation.getUpdatedAt().isAfter(reservation.getCreatedAt()));
    }
}