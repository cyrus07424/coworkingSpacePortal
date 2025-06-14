package forms;

import lombok.Data;
import lombok.NoArgsConstructor;
import models.EquipmentCategory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class EquipmentForm {

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String name;

    @Constraints.Required
    private String purchasePrice;

    @Constraints.MaxLength(1000)
    private String description;

    @Constraints.Required
    private String category;

    public EquipmentForm(String name, BigDecimal purchasePrice, String description, EquipmentCategory category) {
        this.name = name;
        this.purchasePrice = purchasePrice != null ? purchasePrice.toString() : "";
        this.description = description;
        this.category = category != null ? category.name() : "";
    }

    public BigDecimal getPurchasePriceAsDecimal() {
        try {
            return new BigDecimal(purchasePrice);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public EquipmentCategory getCategoryAsEnum() {
        return EquipmentCategory.fromString(category);
    }

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            if (purchasePrice != null && !purchasePrice.isEmpty()) {
                BigDecimal price = new BigDecimal(purchasePrice);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(new ValidationError("purchasePrice", "購入価格は0以上である必要があります"));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(new ValidationError("purchasePrice", "有効な数値を入力してください"));
        }
        
        return errors.isEmpty() ? null : errors;
    }
}