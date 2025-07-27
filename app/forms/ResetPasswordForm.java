package forms;

import play.data.validation.Constraints;

/**
 * Form for resetting password with token
 */
public class ResetPasswordForm {

    @Constraints.Required(message = "トークンは必須です")
    private String token;

    @Constraints.Required(message = "新しいパスワードは必須です")
    @Constraints.MinLength(value = 6, message = "パスワードは6文字以上で入力してください")
    private String newPassword;

    @Constraints.Required(message = "パスワード確認は必須です")
    private String confirmPassword;

    public ResetPasswordForm() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}