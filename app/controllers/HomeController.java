package controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
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
     * index.
     */
    public Result index(Http.Request request) {
        return ok(index.render());
    }
}
