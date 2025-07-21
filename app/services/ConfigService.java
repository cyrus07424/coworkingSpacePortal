package services;

import com.typesafe.config.Config;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Service for handling configuration related to Terms of Service and Privacy Policy URLs
 */
@Singleton
public class ConfigService {

    private final Config config;

    @Inject
    public ConfigService(Config config) {
        this.config = config;
    }

    /**
     * Get the Terms of Service URL from environment variable TERMS_OF_SERVICE_URL
     */
    public Optional<String> getTermsOfServiceUrl() {
        String envValue = System.getenv("TERMS_OF_SERVICE_URL");
        if (envValue != null && !envValue.trim().isEmpty()) {
            return Optional.of(envValue.trim());
        }
        return Optional.empty();
    }

    /**
     * Get the Privacy Policy URL from environment variable PRIVACY_POLICY_URL
     */
    public Optional<String> getPrivacyPolicyUrl() {
        String envValue = System.getenv("PRIVACY_POLICY_URL");
        if (envValue != null && !envValue.trim().isEmpty()) {
            return Optional.of(envValue.trim());
        }
        return Optional.empty();
    }

    /**
     * Check if Terms of Service URL is configured
     */
    public boolean hasTermsOfServiceUrl() {
        return getTermsOfServiceUrl().isPresent();
    }

    /**
     * Check if Privacy Policy URL is configured
     */
    public boolean hasPrivacyPolicyUrl() {
        return getPrivacyPolicyUrl().isPresent();
    }

    /**
     * Check if both URLs are configured (for footer display)
     */
    public boolean hasFooterLinks() {
        return hasTermsOfServiceUrl() || hasPrivacyPolicyUrl();
    }
}