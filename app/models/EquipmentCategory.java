package models;

/**
 * Equipment category enumeration for Single Board Computer related items
 */
public enum EquipmentCategory {
    SINGLE_BOARD_COMPUTER("シングルボードコンピュータ"),
    SENSORS("センサー類"),
    MICROCONTROLLER("マイコン"),
    DEVELOPMENT_BOARD("開発ボード"),
    CABLES("ケーブル類"),
    TOOLS("工具"),
    POWER_SUPPLY("電源"),
    STORAGE("ストレージ"),
    OTHER("その他");

    private final String displayName;

    EquipmentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipmentCategory fromString(String category) {
        if (category == null) {
            return OTHER; // Default category
        }
        try {
            return EquipmentCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER; // Default category for invalid input
        }
    }
}