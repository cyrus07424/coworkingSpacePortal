import org.junit.Test;
import org.junit.Before;
import play.mvc.Result;
import play.mvc.Http;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Test for custom ErrorHandler functionality
 */
public class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @Before
    public void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    public void testServerErrorHandling() throws Exception {
        Http.RequestHeader mockRequest = new Http.RequestBuilder()
            .uri("/test")
            .method("GET")
            .build();
        
        RuntimeException testException = new RuntimeException("Test server error");
        
        CompletionStage<Result> resultStage = errorHandler.onServerError(mockRequest, testException);
        Result result = resultStage.toCompletableFuture().get(1, TimeUnit.SECONDS);
        
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // Test that we get a result with error status
        assertNotNull(result);
    }

    @Test
    public void testClientErrorHandling() throws Exception {
        Http.RequestHeader mockRequest = new Http.RequestBuilder()
            .uri("/test")
            .method("GET")
            .build();
        
        CompletionStage<Result> resultStage = errorHandler.onClientError(mockRequest, 404, "Not Found");
        Result result = resultStage.toCompletableFuture().get(1, TimeUnit.SECONDS);
        
        assertEquals(NOT_FOUND, result.status());
        // Test that we get a result with error status
        assertNotNull(result);
    }
}