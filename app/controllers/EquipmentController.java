package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import forms.EquipmentForm;
import models.Equipment;
import models.EquipmentCategory;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.EquipmentRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Controller for equipment management functionality
 */
public class EquipmentController extends Controller {

    private final EquipmentRepository equipmentRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public EquipmentController(EquipmentRepository equipmentRepository,
                               FormFactory formFactory,
                               ClassLoaderExecutionContext classLoaderExecutionContext,
                               MessagesApi messagesApi) {
        this.equipmentRepository = equipmentRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
    }

    /**
     * Display equipment management page
     */
    @Authenticated
    public CompletionStage<Result> index(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        // Check if user has permission to manage equipment
        if (!currentUser.canManageEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return equipmentRepository.findAll().thenApplyAsync(equipmentList -> {
            return ok(views.html.equipment.index.render(equipmentList, currentUser, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display create equipment form
     */
    @Authenticated
    public Result showCreate(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canManageEquipment()) {
            return Results.redirect(routes.HomeController.index())
                .flashing("error", "このページにアクセスする権限がありません");
        }

        Form<EquipmentForm> form = formFactory.form(EquipmentForm.class);
        return ok(views.html.equipment.create.render(form, EquipmentCategory.values(), currentUser, request, messagesApi.preferred(request)));
    }

    /**
     * Handle create equipment form submission
     */
    @Authenticated
    public CompletionStage<Result> create(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canManageEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        Form<EquipmentForm> form = formFactory.form(EquipmentForm.class).bindFromRequest(request);
        
        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(views.html.equipment.create.render(form, EquipmentCategory.values(), currentUser, request, messagesApi.preferred(request)))
            );
        }

        EquipmentForm equipmentForm = form.get();
        Equipment equipment = new Equipment(
            equipmentForm.getName(),
            equipmentForm.getPurchasePriceAsDecimal(),
            equipmentForm.getDescription(),
            equipmentForm.getCategoryAsEnum()
        );

        return equipmentRepository.insert(equipment).thenApplyAsync(equipmentId -> {
            return Results.redirect(routes.EquipmentController.index())
                .flashing("success", "備品が登録されました");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display edit equipment form
     */
    @Authenticated
    public CompletionStage<Result> showEdit(Http.Request request, Long id) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canManageEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return equipmentRepository.findById(id).thenApplyAsync(equipmentOpt -> {
            if (equipmentOpt.isEmpty()) {
                return Results.redirect(routes.EquipmentController.index())
                    .flashing("error", "指定された備品が見つかりません");
            }

            Equipment equipment = equipmentOpt.get();
            EquipmentForm equipmentForm = new EquipmentForm(
                equipment.getName(),
                equipment.getPurchasePrice(),
                equipment.getDescription(),
                equipment.getCategory()
            );
            
            Form<EquipmentForm> form = formFactory.form(EquipmentForm.class).fill(equipmentForm);
            return ok(views.html.equipment.edit.render(form, equipment, EquipmentCategory.values(), currentUser, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle edit equipment form submission
     */
    @Authenticated
    public CompletionStage<Result> edit(Http.Request request, Long id) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canManageEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return equipmentRepository.findById(id).thenComposeAsync(equipmentOpt -> {
            if (equipmentOpt.isEmpty()) {
                return CompletableFuture.completedFuture(
                    Results.redirect(routes.EquipmentController.index())
                        .flashing("error", "指定された備品が見つかりません")
                );
            }

            Form<EquipmentForm> form = formFactory.form(EquipmentForm.class).bindFromRequest(request);
            Equipment equipment = equipmentOpt.get();
            
            if (form.hasErrors()) {
                return CompletableFuture.completedFuture(
                    badRequest(views.html.equipment.edit.render(form, equipment, EquipmentCategory.values(), currentUser, request, messagesApi.preferred(request)))
                );
            }

            EquipmentForm equipmentForm = form.get();
            equipment.setName(equipmentForm.getName());
            equipment.setPurchasePrice(equipmentForm.getPurchasePriceAsDecimal());
            equipment.setDescription(equipmentForm.getDescription());
            equipment.setCategory(equipmentForm.getCategoryAsEnum());

            return equipmentRepository.update(equipment).thenApplyAsync(updatedEquipment -> {
                return Results.redirect(routes.EquipmentController.index())
                    .flashing("success", "備品が更新されました");
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle equipment deletion
     */
    @Authenticated
    public CompletionStage<Result> delete(Http.Request request, Long id) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canManageEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return equipmentRepository.delete(id).thenApplyAsync(deleted -> {
            if (deleted) {
                return Results.redirect(routes.EquipmentController.index())
                    .flashing("success", "備品が削除されました");
            } else {
                return Results.redirect(routes.EquipmentController.index())
                    .flashing("error", "備品の削除に失敗しました");
            }
        }, classLoaderExecutionContext.current());
    }
}