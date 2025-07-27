package forms;

import play.data.validation.Constraints;

/**
 * Form for forgot password functionality
 */
public class ForgotPasswordForm {

    @Constraints.Required(message = "メールアドレスは必須です")
    @Constraints.Email(message = "有効なメールアドレスを入力してください")
    @Constraints.MaxLength(255)
    private String email;

    public ForgotPasswordForm() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}