package forms;

import play.data.validation.Constraints;

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

    // Optional field for Terms of Service agreement
    private Boolean termsAgreement;

    public RegisterForm() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public Boolean getTermsAgreement() {
        return termsAgreement;
    }

    public void setTermsAgreement(Boolean termsAgreement) {
        this.termsAgreement = termsAgreement;
    }

    /**
     * Check if terms are agreed to (required when Terms of Service URL is configured)
     */
    public boolean isTermsAgreed() {
        return termsAgreement != null && termsAgreement;
    }
}