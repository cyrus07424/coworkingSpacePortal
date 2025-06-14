package models;

/**
 * User role enumeration
 */
public enum UserRole {
    ADMIN("システム管理者"),
    STAFF("店舗スタッフ"),
    CUSTOMER("顧客");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String role) {
        if (role == null) {
            return CUSTOMER; // Default role
        }
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER; // Default role for invalid input
        }
    }
}