package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import forms.CreateStaffForm;
import models.User;
import models.UserRole;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.UserRepository;
import services.SlackNotificationService;
import views.html.user.management;
import views.html.user.createStaff;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Controller for user management functionality
 */
public class UserManagementController extends Controller {

    private final UserRepository userRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public UserManagementController(UserRepository userRepository,
                                    FormFactory formFactory,
                                    ClassLoaderExecutionContext classLoaderExecutionContext,
                                    MessagesApi messagesApi,
                                    SlackNotificationService slackNotificationService) {
        this.userRepository = userRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.slackNotificationService = slackNotificationService;
    }

    /**
     * Display user management page
     */
    @Authenticated
    public CompletionStage<Result> index(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        // Check if user has permission to access user management
        if (!currentUser.canManageUsers()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return userRepository.findAll().thenApplyAsync(users -> {
            return ok(management.render(users, currentUser, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display create staff form (admin only)
     */
    @Authenticated
    public Result showCreateStaff(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        // Only admins can create staff
        if (!currentUser.canCreateStaff()) {
            return Results.redirect(routes.UserManagementController.index())
                .flashing("error", "スタッフを作成する権限がありません");
        }

        Form<CreateStaffForm> createStaffForm = formFactory.form(CreateStaffForm.class);
        return ok(createStaff.render(createStaffForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle create staff form submission (admin only)
     */
    @Authenticated
    public CompletionStage<Result> createStaff(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        // Only admins can create staff
        if (!currentUser.canCreateStaff()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.UserManagementController.index())
                    .flashing("error", "スタッフを作成する権限がありません")
            );
        }

        Form<CreateStaffForm> createStaffForm = formFactory.form(CreateStaffForm.class).bindFromRequest(request);

        if (createStaffForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(createStaff.render(createStaffForm, request, messagesApi.preferred(request)))
            );
        }

        CreateStaffForm data = createStaffForm.get();

        // Check if username already exists
        return userRepository.existsByUsername(data.getUsername()).thenComposeAsync(usernameExists -> {
            if (usernameExists) {
                return CompletableFuture.completedFuture(
                    badRequest(createStaff.render(
                        createStaffForm.withError("username", "このユーザー名は既に使用されています"),
                        request,
                        messagesApi.preferred(request)
                    ))
                );
            }

            return userRepository.existsByEmail(data.getEmail()).thenComposeAsync(emailExists -> {
                if (emailExists) {
                    return CompletableFuture.completedFuture(
                        badRequest(createStaff.render(
                            createStaffForm.withError("email", "このメールアドレスは既に使用されています"),
                            request,
                            messagesApi.preferred(request)
                        ))
                    );
                }

                // Create new staff user
                User user = new User(data.getUsername(), data.getEmail(), data.getPassword(), UserRole.STAFF);
                return userRepository.insert(user).thenApplyAsync(userId -> {
                    // Send Slack notification for staff creation (fire and forget)
                    slackNotificationService.notifyUserUpdate(currentUser, user, "スタッフユーザー作成", request);
                    
                    return Results.redirect(routes.UserManagementController.index())
                        .flashing("success", "スタッフユーザーが作成されました");
                }, classLoaderExecutionContext.current());
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }
}