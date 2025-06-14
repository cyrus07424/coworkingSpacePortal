package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.common.dashboard;
import views.html.common.index;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public HomeController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    /**
     * Display index or dashboard based on authentication status
     */
    public Result index(Http.Request request) {
        // Check if user is authenticated
        if (request.session().get("userId").isPresent()) {
            return dashboard(request);
        } else {
            return ok(index.render());
        }
    }

    /**
     * Display dashboard for authenticated users
     */
    @Authenticated
    public Result dashboard(Http.Request request) {
        User user = request.attrs().get(AuthenticatedAction.USER_KEY);
        return ok(dashboard.render(user));
    }
}
