import org.junit.Test;
import play.test.WithBrowser;

import static io.fluentlenium.core.filter.FilterConstructor.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BrowserTest extends WithBrowser {

  @Test
  public void testBrowser() {
    browser.goTo("http://localhost:" + port);
  }
}