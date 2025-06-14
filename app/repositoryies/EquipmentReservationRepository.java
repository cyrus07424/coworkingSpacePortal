package repositoryies;

import io.ebean.DB;
import models.Equipment;
import models.EquipmentReservation;
import models.User;
import services.DatabaseExecutionContext;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes equipment reservation database operations in a different
 * execution context.
 */
public class EquipmentReservationRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public EquipmentReservationRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<EquipmentReservation>> findById(Long id) {
        return supplyAsync(() -> DB.find(EquipmentReservation.class)
                .fetch("equipment")
                .fetch("user")
                .setId(id)
                .findOneOrEmpty(), executionContext);
    }

    public CompletionStage<List<EquipmentReservation>> findByUser(User user) {
        return supplyAsync(() -> DB.find(EquipmentReservation.class)
                .fetch("equipment")
                .where()
                .eq("user", user)
                .orderBy("reservationDate desc")
                .findList(), executionContext);
    }

    public CompletionStage<List<EquipmentReservation>> findActiveReservations() {
        return supplyAsync(() -> DB.find(EquipmentReservation.class)
                .fetch("equipment")
                .fetch("user")
                .where()
                .eq("status", EquipmentReservation.ReservationStatus.ACTIVE)
                .orderBy("reservationDate desc")
                .findList(), executionContext);
    }

    public CompletionStage<Boolean> isEquipmentAvailable(Equipment equipment, LocalDate date) {
        return supplyAsync(() -> {
            long count = DB.find(EquipmentReservation.class)
                    .where()
                    .eq("equipment", equipment)
                    .eq("reservationDate", date)
                    .eq("status", EquipmentReservation.ReservationStatus.ACTIVE)
                    .findCount();
            return count == 0;
        }, executionContext);
    }

    public CompletionStage<Long> insert(EquipmentReservation reservation) {
        return supplyAsync(() -> {
            reservation.save();
            return reservation.getId();
        }, executionContext);
    }

    public CompletionStage<EquipmentReservation> update(EquipmentReservation reservation) {
        return supplyAsync(() -> {
            reservation.update();
            return reservation;
        }, executionContext);
    }

    public CompletionStage<Boolean> cancelReservation(Long id, User user) {
        return supplyAsync(() -> {
            Optional<EquipmentReservation> reservation = DB.find(EquipmentReservation.class)
                    .where()
                    .eq("id", id)
                    .eq("user", user)
                    .eq("status", EquipmentReservation.ReservationStatus.ACTIVE)
                    .findOneOrEmpty();
            
            if (reservation.isPresent()) {
                EquipmentReservation res = reservation.get();
                res.setStatus(EquipmentReservation.ReservationStatus.CANCELLED);
                res.update();
                return true;
            }
            return false;
        }, executionContext);
    }
}