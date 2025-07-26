import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import views.html.error.error;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Custom error handler to display a common error page when system errors occur
 */
@Singleton
public class ErrorHandler implements play.http.HttpErrorHandler {

    @Inject
    public ErrorHandler() {
    }

    @Override
    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        // Handle client errors (4xx)
        return CompletableFuture.completedFuture(
            Results.status(statusCode, error.render(statusCode, message))
        );
    }

    @Override
    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        // Handle server errors (5xx) - this is the main requirement
        return CompletableFuture.completedFuture(
            Results.internalServerError(error.render(500, "システムエラーが発生しました"))
        );
    }
}