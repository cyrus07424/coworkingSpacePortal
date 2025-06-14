package forms;

import lombok.Data;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateStaffForm {

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

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (!passwordsMatch()) {
            errors.add(new ValidationError("confirmPassword", "パスワードが一致しません"));
        }
        return errors.isEmpty() ? null : errors;
    }
}