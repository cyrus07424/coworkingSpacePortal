package models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import play.data.validation.Constraints;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password reset token entity managed by Ebean
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Constraints.Required
    private LocalDateTime expiresAt;

    private boolean used;

    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusHours(24); // Valid for 24 hours
        this.used = false;
    }

    public PasswordResetToken(User user) {
        this();
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }
}