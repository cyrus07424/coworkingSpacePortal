package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import models.User;
import play.libs.concurrent.ClassLoaderExecutionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service for sending emails via SMTP
 */
@Singleton
public class EmailService {

    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromEmail;
    private final String fromName;
    private final boolean smtpAuth;
    private final boolean smtpStartTls;

    @Inject
    public EmailService(ClassLoaderExecutionContext classLoaderExecutionContext) {
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.smtpHost = System.getenv("SMTP_HOST");
        this.smtpPort = System.getenv().getOrDefault("SMTP_PORT", "587");
        this.smtpUsername = System.getenv("SMTP_USERNAME");
        this.smtpPassword = System.getenv("SMTP_PASSWORD");
        this.fromEmail = System.getenv().getOrDefault("FROM_EMAIL", "noreply@coworkingspace.local");
        this.fromName = System.getenv().getOrDefault("FROM_NAME", "コワーキングスペースポータル");
        this.smtpAuth = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_AUTH", "true"));
        this.smtpStartTls = Boolean.parseBoolean(System.getenv().getOrDefault("SMTP_STARTTLS", "true"));
    }

    /**
     * Check if email service is properly configured
     */
    public boolean isConfigured() {
        return smtpHost != null && !smtpHost.trim().isEmpty() &&
               smtpUsername != null && !smtpUsername.trim().isEmpty() &&
               smtpPassword != null && !smtpPassword.trim().isEmpty();
    }

    /**
     * Send welcome email to newly registered user
     */
    public CompletionStage<Boolean> sendWelcomeEmail(User user) {
        if (!isConfigured()) {
            System.out.println("Email service not configured. Skipping welcome email for user: " + user.getUsername());
            return CompletableFuture.completedFuture(false);
        }

        String subject = "コワーキングスペースポータルへようこそ";
        String body = String.format(
            "こんにちは %s さん,\n\n" +
            "コワーキングスペースポータルへの会員登録が完了しました。\n\n" +
            "ユーザー名: %s\n" +
            "メールアドレス: %s\n\n" +
            "ポータルサイトにログインして、設備の予約や管理を行うことができます。\n\n" +
            "ご不明な点がございましたら、お気軽にお問い合わせください。\n\n" +
            "コワーキングスペースポータル",
            user.getUsername(),
            user.getUsername(),
            user.getEmail()
        );

        return sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send password reset email
     */
    public CompletionStage<Boolean> sendPasswordResetEmail(User user, String token, String baseUrl) {
        if (!isConfigured()) {
            System.out.println("Email service not configured. Skipping password reset email for user: " + user.getUsername());
            return CompletableFuture.completedFuture(false);
        }

        String resetUrl = baseUrl + "/reset-password?token=" + token;
        String subject = "パスワードリセットのご案内";
        String body = String.format(
            "こんにちは %s さん,\n\n" +
            "パスワードリセットのリクエストを受け付けました。\n\n" +
            "以下のリンクをクリックして、新しいパスワードを設定してください。\n" +
            "このリンクは24時間で無効になります。\n\n" +
            "%s\n\n" +
            "このメールに心当たりがない場合は、このメールを無視してください。\n\n" +
            "コワーキングスペースポータル",
            user.getUsername(),
            resetUrl
        );

        return sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Send email using SMTP
     */
    private CompletionStage<Boolean> sendEmail(String toEmail, String subject, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.auth", smtpAuth);
                props.put("mail.smtp.starttls.enable", smtpStartTls);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("Email sent successfully to: " + toEmail);
                return true;

            } catch (Exception e) {
                System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, classLoaderExecutionContext.current());
    }
}