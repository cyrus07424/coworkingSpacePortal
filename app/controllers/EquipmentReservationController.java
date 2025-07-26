package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import forms.EquipmentReservationForm;
import models.Equipment;
import models.EquipmentReservation;
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
import repositoryies.EquipmentReservationRepository;
import services.SlackNotificationService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Controller for equipment reservation functionality
 */
public class EquipmentReservationController extends Controller {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentReservationRepository reservationRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public EquipmentReservationController(EquipmentRepository equipmentRepository,
                                          EquipmentReservationRepository reservationRepository,
                                          FormFactory formFactory,
                                          ClassLoaderExecutionContext classLoaderExecutionContext,
                                          MessagesApi messagesApi,
                                          SlackNotificationService slackNotificationService) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.slackNotificationService = slackNotificationService;
    }

    /**
     * Display equipment reservation page
     */
    @Authenticated
    public CompletionStage<Result> index(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canReserveEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return equipmentRepository.findAll().thenComposeAsync(equipmentList -> {
            return reservationRepository.findByUser(currentUser).thenApplyAsync(reservations -> {
                Form<EquipmentReservationForm> form = formFactory.form(EquipmentReservationForm.class);
                return ok(views.html.equipment.reservation.index.render(
                    equipmentList, reservations, form, currentUser, request, messagesApi.preferred(request)
                ));
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle equipment reservation form submission
     */
    @Authenticated
    public CompletionStage<Result> reserve(Http.Request request) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canReserveEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        Form<EquipmentReservationForm> form = formFactory.form(EquipmentReservationForm.class).bindFromRequest(request);
        
        if (form.hasErrors()) {
            return equipmentRepository.findAll().thenComposeAsync(equipmentList -> {
                return reservationRepository.findByUser(currentUser).thenApplyAsync(reservations -> {
                    return badRequest(views.html.equipment.reservation.index.render(
                        equipmentList, reservations, form, currentUser, request, messagesApi.preferred(request)
                    ));
                }, classLoaderExecutionContext.current());
            }, classLoaderExecutionContext.current());
        }

        EquipmentReservationForm reservationForm = form.get();
        Long equipmentId = reservationForm.getEquipmentIdAsLong();
        
        if (equipmentId == null) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.EquipmentReservationController.index())
                    .flashing("error", "有効な備品を選択してください")
            );
        }

        return equipmentRepository.findById(equipmentId).thenComposeAsync(equipmentOpt -> {
            if (equipmentOpt.isEmpty()) {
                return CompletableFuture.completedFuture(
                    Results.redirect(routes.EquipmentReservationController.index())
                        .flashing("error", "指定された備品が見つかりません")
                );
            }

            Equipment equipment = equipmentOpt.get();
            
            // Check if equipment is available on the requested date
            return reservationRepository.isEquipmentAvailable(equipment, reservationForm.getReservationDateAsLocalDate())
                .thenComposeAsync(isAvailable -> {
                    if (!isAvailable) {
                        return CompletableFuture.completedFuture(
                            Results.redirect(routes.EquipmentReservationController.index())
                                .flashing("error", "選択された日付は既に予約済みです")
                        );
                    }

                    EquipmentReservation reservation = new EquipmentReservation(
                        equipment,
                        currentUser,
                        reservationForm.getReservationDateAsLocalDate()
                    );

                    return reservationRepository.insert(reservation).thenApplyAsync(reservationId -> {
                        return Results.redirect(routes.EquipmentReservationController.index())
                            .flashing("success", "備品の予約が完了しました");
                    }, classLoaderExecutionContext.current());
                    
                }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle reservation cancellation
     */
    @Authenticated
    public CompletionStage<Result> cancel(Http.Request request, Long id) {
        User currentUser = request.attrs().get(AuthenticatedAction.USER_KEY);
        
        if (!currentUser.canReserveEquipment()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.HomeController.index())
                    .flashing("error", "このページにアクセスする権限がありません")
            );
        }

        return reservationRepository.cancelReservation(id, currentUser).thenApplyAsync(cancelled -> {
            if (cancelled) {
                return Results.redirect(routes.EquipmentReservationController.index())
                    .flashing("success", "予約がキャンセルされました");
            } else {
                return Results.redirect(routes.EquipmentReservationController.index())
                    .flashing("error", "予約のキャンセルに失敗しました");
            }
        }, classLoaderExecutionContext.current());
    }
}