package forms;

import lombok.Data;
import play.data.validation.Constraints;

@Data
public class LoginForm {

    @Constraints.Required
    private String username;

    @Constraints.Required
    private String password;
}