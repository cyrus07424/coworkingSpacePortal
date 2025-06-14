package models;

import jakarta.persistence.*;
import play.data.validation.Constraints;

import java.time.LocalDate;

/**
 * Equipment reservation entity managed by Ebean
 */
@Entity
@Table(name = "equipment_reservation")
public class EquipmentReservation extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Constraints.Required
    private LocalDate reservationDate;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public enum ReservationStatus {
        ACTIVE("予約中"),
        CANCELLED("キャンセル済み");

        private final String displayName;

        ReservationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public EquipmentReservation() {
        this.status = ReservationStatus.ACTIVE; // Default status
    }

    public EquipmentReservation(Equipment equipment, User user, LocalDate reservationDate) {
        this.equipment = equipment;
        this.user = user;
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.ACTIVE;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public ReservationStatus getStatus() {
        return status != null ? status : ReservationStatus.ACTIVE;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return ReservationStatus.ACTIVE.equals(getStatus());
    }

    public boolean isCancelled() {
        return ReservationStatus.CANCELLED.equals(getStatus());
    }
}