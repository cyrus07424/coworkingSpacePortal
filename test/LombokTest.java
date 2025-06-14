import org.junit.Test;
import utils.TestLombok;

import static org.junit.Assert.assertEquals;

public class LombokTest {

    @Test
    public void testLombokGetterSetter() {
        TestLombok test = new TestLombok();
        test.setName("Test Name");
        test.setAge(25);
        
        assertEquals("Test Name", test.getName());
        assertEquals(Integer.valueOf(25), test.getAge());
    }
}