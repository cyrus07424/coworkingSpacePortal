package models;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import org.mindrot.jbcrypt.BCrypt;
import play.data.validation.Constraints;

/**
 * User entity managed by Ebean
 */
@Entity
@Table(name = "app_user")
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String username;

    @Constraints.Required
    @Constraints.Email
    @Constraints.MaxLength(255)
    private String email;

    @Constraints.Required
    @Constraints.MinLength(6)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public User() {
        this.role = UserRole.CUSTOMER; // Default role
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.setPassword(password);
        this.role = UserRole.CUSTOMER; // Default role for new users
    }

    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.setPassword(password);
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public UserRole getRole() {
        return role != null ? role : UserRole.CUSTOMER;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(getRole());
    }

    public boolean isStaff() {
        return UserRole.STAFF.equals(getRole());
    }

    public boolean isCustomer() {
        return UserRole.CUSTOMER.equals(getRole());
    }

    public boolean canManageUsers() {
        return isAdmin() || isStaff();
    }

    public boolean canCreateStaff() {
        return isAdmin();
    }

    public boolean canManageEquipment() {
        return isAdmin() || isStaff();
    }

    public boolean canReserveEquipment() {
        return isCustomer();
    }
}