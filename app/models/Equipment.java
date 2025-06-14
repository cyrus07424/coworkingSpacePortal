package models;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import play.data.validation.Constraints;

import java.math.BigDecimal;

/**
 * Equipment entity managed by Ebean
 */
@Entity
@Table(name = "equipment")
public class Equipment extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String name;

    @Constraints.Required
    private BigDecimal purchasePrice;

    @Constraints.MaxLength(1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private EquipmentCategory category;

    public Equipment() {
        this.category = EquipmentCategory.OTHER; // Default category
    }

    public Equipment(String name, BigDecimal purchasePrice, String description, EquipmentCategory category) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EquipmentCategory getCategory() {
        return category != null ? category : EquipmentCategory.OTHER;
    }

    public void setCategory(EquipmentCategory category) {
        this.category = category;
    }
}