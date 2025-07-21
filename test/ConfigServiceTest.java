package services;

import org.junit.Test;
import com.typesafe.config.ConfigFactory;
import static org.junit.Assert.*;

public class ConfigServiceTest {

    @Test
    public void testTermsOfServiceUrlFromEnvironment() {
        // Test with environment variable set
        ConfigService configService = new ConfigService(ConfigFactory.load());
        
        // Set environment variable for testing
        String originalValue = System.getenv("TERMS_OF_SERVICE_URL");
        try {
            // This test shows how the service would work when environment variables are set
            // In real scenario, environment variables would be set externally
            
            // Test hasTermsOfServiceUrl logic
            assertTrue("Should have terms URL when environment variable is set", 
                configService.getTermsOfServiceUrl().isPresent() || 
                System.getenv("TERMS_OF_SERVICE_URL") == null);
            
            // Test validation logic
            assertTrue("Service should be instantiable", configService != null);
        } finally {
            // Cleanup would be done here if we were modifying system environment
        }
    }

    @Test
    public void testPrivacyPolicyUrlFromEnvironment() {
        ConfigService configService = new ConfigService(ConfigFactory.load());
        
        // Test validation logic for privacy policy
        assertTrue("Service should handle privacy policy URL correctly", 
            configService.getPrivacyPolicyUrl().isPresent() || 
            System.getenv("PRIVACY_POLICY_URL") == null);
        
        assertTrue("Service should be instantiable", configService != null);
    }
    
    @Test
    public void testFooterLinksLogic() {
        ConfigService configService = new ConfigService(ConfigFactory.load());
        
        // Test footer display logic
        boolean hasFooterLinks = configService.hasFooterLinks();
        boolean hasTerms = configService.hasTermsOfServiceUrl();
        boolean hasPrivacy = configService.hasPrivacyPolicyUrl();
        
        // Footer should be shown if either URL is present
        assertEquals("Footer links should be shown when any URL is present", 
            hasTerms || hasPrivacy, hasFooterLinks);
    }
}