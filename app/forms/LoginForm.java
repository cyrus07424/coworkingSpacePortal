package forms;

import play.data.validation.Constraints;

public class LoginForm {

    @Constraints.Required
    private String username;

    @Constraints.Required
    private String password;

    public LoginForm() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}