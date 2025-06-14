import controllers.routes;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

// Use FixMethodOrder to run the tests sequentially
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FunctionalTest extends WithApplication {

    @Test
    public void redirectHomePage() {
        Result result = route(app, controllers.routes.HomeController.index());

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).isEqualTo("/");
    }
}