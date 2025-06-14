package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentReservationForm {

    @Constraints.Required
    private String equipmentId;

    @Constraints.Required
    private String reservationDate;

    public EquipmentReservationForm() {
    }

    public EquipmentReservationForm(Long equipmentId, LocalDate reservationDate) {
        this.equipmentId = equipmentId != null ? equipmentId.toString() : "";
        this.reservationDate = reservationDate != null ? reservationDate.toString() : "";
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Long getEquipmentIdAsLong() {
        try {
            return Long.parseLong(equipmentId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public LocalDate getReservationDateAsLocalDate() {
        try {
            return LocalDate.parse(reservationDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        // Validate equipment ID
        try {
            if (equipmentId != null && !equipmentId.isEmpty()) {
                Long.parseLong(equipmentId);
            }
        } catch (NumberFormatException e) {
            errors.add(new ValidationError("equipmentId", "有効な備品を選択してください"));
        }

        // Validate reservation date
        try {
            if (reservationDate != null && !reservationDate.isEmpty()) {
                LocalDate date = LocalDate.parse(reservationDate, DateTimeFormatter.ISO_LOCAL_DATE);
                if (date.isBefore(LocalDate.now())) {
                    errors.add(new ValidationError("reservationDate", "予約日は今日以降の日付を選択してください"));
                }
            }
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError("reservationDate", "有効な日付を入力してください"));
        }

        return errors.isEmpty() ? null : errors;
    }
}