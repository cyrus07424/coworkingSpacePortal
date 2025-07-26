package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service for sending notifications to Slack webhooks
 */
@Singleton
public class SlackNotificationService {

    private final WSClient wsClient;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final String slackWebhookUrl;
    private final ObjectMapper objectMapper;

    @Inject
    public SlackNotificationService(WSClient wsClient, 
                                   ClassLoaderExecutionContext classLoaderExecutionContext) {
        this.wsClient = wsClient;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.slackWebhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Check if Slack notifications are enabled (webhook URL is configured)
     */
    public boolean isEnabled() {
        return slackWebhookUrl != null && !slackWebhookUrl.trim().isEmpty();
    }

    /**
     * Send notification for user registration
     */
    public CompletionStage<Void> notifyUserRegistration(User user, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "🆕 *新規ユーザー登録*\n" +
            "ユーザー名: %s\n" +
            "メールアドレス: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            user.getUsername(),
            user.getEmail(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send notification for user login
     */
    public CompletionStage<Void> notifyUserLogin(User user, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "🔑 *ユーザーログイン*\n" +
            "ユーザー名: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            user.getUsername(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send notification for user data update
     */
    public CompletionStage<Void> notifyUserUpdate(User currentUser, User targetUser, String action, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "👤 *ユーザー情報更新*\n" +
            "操作: %s\n" +
            "対象ユーザー: %s\n" +
            "実行者: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            action,
            targetUser.getUsername(),
            currentUser.getUsername(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send notification for equipment operations
     */
    public CompletionStage<Void> notifyEquipmentOperation(User user, String equipmentName, String action, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "🔧 *機材操作*\n" +
            "操作: %s\n" +
            "機材: %s\n" +
            "ユーザー: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            action,
            equipmentName,
            user.getUsername(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send notification for equipment reservation
     */
    public CompletionStage<Void> notifyEquipmentReservation(User user, String equipmentName, String action, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "📅 *機材予約*\n" +
            "操作: %s\n" +
            "機材: %s\n" +
            "ユーザー: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            action,
            equipmentName,
            user.getUsername(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send a generic notification message
     */
    public CompletionStage<Void> notifyGeneric(String action, User user, Http.Request request) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        String message = String.format(
            "ℹ️ *システム操作*\n" +
            "操作: %s\n" +
            "ユーザー: %s\n" +
            "IP アドレス: %s\n" +
            "User Agent: %s",
            action,
            user.getUsername(),
            getClientIpAddress(request),
            getUserAgent(request)
        );

        return sendSlackMessage(message);
    }

    /**
     * Send message to Slack webhook
     */
    private CompletionStage<Void> sendSlackMessage(String message) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("text", message);

            WSRequest request = wsClient.url(slackWebhookUrl)
                    .setContentType("application/json");

            return request.post(payload)
                    .thenApplyAsync(response -> {
                        if (response.getStatus() != 200) {
                            // Log error but don't fail the main operation
                            System.err.println("Failed to send Slack notification: " + response.getStatus() + " - " + response.getBody());
                        }
                        return (Void) null;
                    }, classLoaderExecutionContext.current())
                    .exceptionally(throwable -> {
                        // Log error but don't fail the main operation
                        System.err.println("Exception sending Slack notification: " + throwable.getMessage());
                        return (Void) null;
                    });
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Exception creating Slack notification: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(Http.Request request) {
        // Check for X-Forwarded-For header (common in load balancers/proxies)
        String xForwardedFor = request.header("X-Forwarded-For").orElse(null);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String xRealIp = request.header("X-Real-IP").orElse(null);
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback to remote address
        return request.remoteAddress();
    }

    /**
     * Extract User Agent from request
     */
    private String getUserAgent(Http.Request request) {
        return request.header("User-Agent").orElse("Unknown");
    }
}