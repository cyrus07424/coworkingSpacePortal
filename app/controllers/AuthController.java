package controllers;

import forms.ForgotPasswordForm;
import forms.LoginForm;
import forms.RegisterForm;
import forms.ResetPasswordForm;
import models.PasswordResetToken;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.PasswordResetTokenRepository;
import repositoryies.UserRepository;
import services.ConfigService;
import services.EmailService;
import services.SlackNotificationService;
import views.html.auth.forgotPassword;
import views.html.auth.login;
import views.html.auth.register;
import views.html.auth.resetPassword;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller handles authentication related actions
 */
public class AuthController extends Controller {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final ConfigService configService;
    private final SlackNotificationService slackNotificationService;
    private final EmailService emailService;

    @Inject
    public AuthController(UserRepository userRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          FormFactory formFactory,
                          ClassLoaderExecutionContext classLoaderExecutionContext,
                          MessagesApi messagesApi,
                          ConfigService configService,
                          SlackNotificationService slackNotificationService,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.configService = configService;
        this.slackNotificationService = slackNotificationService;
        this.emailService = emailService;
    }

    /**
     * Display the login form
     */
    public Result showLogin(Http.Request request) {
        // If user is already logged in, redirect to index
        if (request.session().get("userId").isPresent()) {
            return Results.redirect(routes.HomeController.index());
        }

        Form<LoginForm> loginForm = formFactory.form(LoginForm.class);
        return ok(login.render(loginForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle login form submission
     */
    public CompletionStage<Result> login(Http.Request request) {
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                    badRequest(login.render(loginForm, request, messagesApi.preferred(request)))
            );
        }

        LoginForm data = loginForm.get();

        return userRepository.findByUsername(data.getUsername()).thenApplyAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.checkPassword(data.getPassword())) {
                    // Send Slack notification for login (fire and forget)
                    slackNotificationService.notifyUserLogin(user, request);
                    
                    return Results.redirect(routes.HomeController.index())
                            .addingToSession(request, "userId", user.getId().toString())
                            .flashing("success", "ログインしました");
                }
            }

            return badRequest(login.render(
                    loginForm.withError("username", "ユーザー名またはパスワードが間違っています"),
                    request,
                    messagesApi.preferred(request)
            ));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the registration form
     */
    public Result showRegister(Http.Request request) {
        // If user is already logged in, redirect to index
        if (request.session().get("userId").isPresent()) {
            return Results.redirect(routes.HomeController.index());
        }

        Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class);
        return ok(register.render(registerForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle registration form submission
     */
    public CompletionStage<Result> register(Http.Request request) {
        Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class).bindFromRequest(request);

        if (registerForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                    badRequest(register.render(registerForm, request, messagesApi.preferred(request)))
            );
        }

        RegisterForm data = registerForm.get();

        // Check if Terms of Service agreement is required and provided
        if (configService.hasTermsOfServiceUrl() && !data.isTermsAgreed()) {
            return CompletableFuture.completedFuture(
                    badRequest(register.render(
                            registerForm.withError("termsAgreement", "利用規約に同意してください"),
                            request,
                            messagesApi.preferred(request)
                    ))
            );
        }

        // Check if passwords match
        if (!data.passwordsMatch()) {
            return CompletableFuture.completedFuture(
                    badRequest(register.render(
                            registerForm.withError("confirmPassword", "パスワードが一致しません"),
                            request,
                            messagesApi.preferred(request)
                    ))
            );
        }

        // Check if username or email already exists
        return userRepository.existsByUsername(data.getUsername()).thenComposeAsync(usernameExists -> {
            if (usernameExists) {
                return CompletableFuture.completedFuture(
                        badRequest(register.render(
                                registerForm.withError("username", "このユーザー名は既に使用されています"),
                                request,
                                messagesApi.preferred(request)
                        ))
                );
            }

            return userRepository.existsByEmail(data.getEmail()).thenComposeAsync(emailExists -> {
                if (emailExists) {
                    return CompletableFuture.completedFuture(
                            badRequest(register.render(
                                    registerForm.withError("email", "このメールアドレスは既に使用されています"),
                                    request,
                                    messagesApi.preferred(request)
                            ))
                    );
                }

                // Create new user
                User user = new User(data.getUsername(), data.getEmail(), data.getPassword());
                return userRepository.insert(user).thenComposeAsync(userId -> {
                    // Send Slack notification for registration (fire and forget)
                    slackNotificationService.notifyUserRegistration(user, request);
                    
                    // Send welcome email (fire and forget)
                    emailService.sendWelcomeEmail(user);
                    
                    return CompletableFuture.completedFuture(
                        Results.redirect(routes.HomeController.index())
                                .addingToSession(request, "userId", userId.toString())
                                .flashing("success", "アカウントが作成されました")
                    );
                }, classLoaderExecutionContext.current());
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle logout
     */
    public Result logout(Http.Request request) {
        return Results.redirect(routes.AuthController.showLogin())
                .removingFromSession(request, "userId")
                .flashing("success", "ログアウトしました");
    }

    /**
     * Display the forgot password form
     */
    public Result showForgotPassword(Http.Request request) {
        // If user is already logged in, redirect to index
        if (request.session().get("userId").isPresent()) {
            return Results.redirect(routes.HomeController.index());
        }

        Form<ForgotPasswordForm> forgotPasswordForm = formFactory.form(ForgotPasswordForm.class);
        return ok(forgotPassword.render(forgotPasswordForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle forgot password form submission
     */
    public CompletionStage<Result> processForgotPassword(Http.Request request) {
        Form<ForgotPasswordForm> forgotPasswordForm = formFactory.form(ForgotPasswordForm.class).bindFromRequest(request);

        if (forgotPasswordForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                    badRequest(forgotPassword.render(forgotPasswordForm, request, messagesApi.preferred(request)))
            );
        }

        ForgotPasswordForm data = forgotPasswordForm.get();
        
        return userRepository.findByEmail(data.getEmail()).thenComposeAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Invalidate existing tokens for this user
                return passwordResetTokenRepository.invalidateUserTokens(user).thenComposeAsync(v -> {
                    // Create new password reset token
                    PasswordResetToken token = new PasswordResetToken(user);
                    
                    return passwordResetTokenRepository.insert(token).thenComposeAsync(tokenId -> {
                        // Get base URL from request
                        String baseUrl = request.secure() ? "https://" : "http://";
                        baseUrl += request.host();
                        
                        // Send password reset email (fire and forget)
                        emailService.sendPasswordResetEmail(user, token.getToken(), baseUrl);
                        
                        return CompletableFuture.completedFuture(
                            Results.redirect(routes.AuthController.showLogin())
                                    .flashing("success", "パスワードリセット用のメールを送信しました。メールをご確認ください。")
                        );
                    }, classLoaderExecutionContext.current());
                }, classLoaderExecutionContext.current());
            } else {
                // For security, don't reveal if email exists or not
                return CompletableFuture.completedFuture(
                    Results.redirect(routes.AuthController.showLogin())
                            .flashing("success", "パスワードリセット用のメールを送信しました。メールをご確認ください。")
                );
            }
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the reset password form
     */
    public CompletionStage<Result> showResetPassword(Http.Request request, String token) {
        // If user is already logged in, redirect to index
        if (request.session().get("userId").isPresent()) {
            return CompletableFuture.completedFuture(Results.redirect(routes.HomeController.index()));
        }

        return passwordResetTokenRepository.findValidToken(token).thenApplyAsync(tokenOptional -> {
            if (tokenOptional.isPresent() && tokenOptional.get().isValid()) {
                Form<ResetPasswordForm> resetPasswordForm = formFactory.form(ResetPasswordForm.class);
                // Pre-fill the token
                resetPasswordForm = resetPasswordForm.fill(new ResetPasswordForm() {{
                    setToken(token);
                }});
                return ok(resetPassword.render(resetPasswordForm, request, messagesApi.preferred(request)));
            } else {
                return Results.redirect(routes.AuthController.showLogin())
                        .flashing("error", "無効または期限切れのパスワードリセットリンクです。");
            }
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle reset password form submission
     */
    public CompletionStage<Result> processResetPassword(Http.Request request) {
        Form<ResetPasswordForm> resetPasswordForm = formFactory.form(ResetPasswordForm.class).bindFromRequest(request);

        if (resetPasswordForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                    badRequest(resetPassword.render(resetPasswordForm, request, messagesApi.preferred(request)))
            );
        }

        ResetPasswordForm data = resetPasswordForm.get();

        // Check if passwords match
        if (!data.passwordsMatch()) {
            return CompletableFuture.completedFuture(
                    badRequest(resetPassword.render(
                            resetPasswordForm.withError("confirmPassword", "パスワードが一致しません"),
                            request,
                            messagesApi.preferred(request)
                    ))
            );
        }

        return passwordResetTokenRepository.findValidToken(data.getToken()).thenComposeAsync(tokenOptional -> {
            if (tokenOptional.isPresent() && tokenOptional.get().isValid()) {
                PasswordResetToken resetToken = tokenOptional.get();
                User user = resetToken.getUser();
                
                // Update user password
                user.setPassword(data.getNewPassword());
                
                // Mark token as used
                resetToken.markAsUsed();
                
                return userRepository.update(user).thenComposeAsync(v -> {
                    return passwordResetTokenRepository.update(resetToken).thenApplyAsync(v2 -> {
                        return Results.redirect(routes.AuthController.showLogin())
                                .flashing("success", "パスワードが正常に更新されました。新しいパスワードでログインしてください。");
                    }, classLoaderExecutionContext.current());
                }, classLoaderExecutionContext.current());
            } else {
                return CompletableFuture.completedFuture(
                    Results.redirect(routes.AuthController.showLogin())
                            .flashing("error", "無効または期限切れのパスワードリセットリンクです。")
                );
            }
        }, classLoaderExecutionContext.current());
    }
}