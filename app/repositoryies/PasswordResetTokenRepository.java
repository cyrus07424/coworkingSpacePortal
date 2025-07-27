package repositoryies;

import io.ebean.DB;
import models.PasswordResetToken;
import models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Repository for PasswordResetToken entities
 */
@Singleton
public class PasswordResetTokenRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public PasswordResetTokenRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Find valid token by token string
     */
    public CompletionStage<Optional<PasswordResetToken>> findValidToken(String token) {
        return supplyAsync(() -> {
            return DB.find(PasswordResetToken.class)
                    .where()
                    .eq("token", token)
                    .eq("used", false)
                    .findOneOrEmpty();
        }, executionContext);
    }

    /**
     * Insert new password reset token
     */
    public CompletionStage<Long> insert(PasswordResetToken token) {
        return supplyAsync(() -> {
            token.save();
            return token.getId();
        }, executionContext);
    }

    /**
     * Update password reset token
     */
    public CompletionStage<Void> update(PasswordResetToken token) {
        return supplyAsync(() -> {
            token.update();
            return null;
        }, executionContext);
    }

    /**
     * Delete expired tokens for cleanup
     */
    public CompletionStage<Integer> deleteExpiredTokens() {
        return supplyAsync(() -> {
            return DB.find(PasswordResetToken.class)
                    .where()
                    .lt("expiresAt", java.time.LocalDateTime.now())
                    .delete();
        }, executionContext);
    }

    /**
     * Invalidate all existing tokens for a user
     */
    public CompletionStage<Void> invalidateUserTokens(User user) {
        return supplyAsync(() -> {
            DB.sqlUpdate("UPDATE password_reset_token SET used = true WHERE user_id = ?")
                    .setParameter(user.getId())
                    .execute();
            return null;
        }, executionContext);
    }
}