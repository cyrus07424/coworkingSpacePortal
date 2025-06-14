package forms;

import lombok.Data;
import play.data.validation.Constraints;

@Data
public class RegisterForm {

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String username;

    @Constraints.Required
    @Constraints.Email
    @Constraints.MaxLength(255)
    private String email;

    @Constraints.Required
    @Constraints.MinLength(6)
    private String password;

    @Constraints.Required
    private String confirmPassword;

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}