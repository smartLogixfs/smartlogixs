package cl.smartlogix.auth.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

    @Test
    void passwordEncoderShouldEncodePassword() {

        SecurityConfig config =
                new SecurityConfig();

        PasswordEncoder encoder =
                config.passwordEncoder();

        String encoded =
                encoder.encode("Password123");

        assertNotNull(encoded);
        assertTrue(
                encoder.matches(
                        "Password123",
                        encoded
                )
        );
    }
}