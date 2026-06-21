package cl.smartlogix.user.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

    @Test
    void debeConstruirFilterChainSinErrores() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);

        when(httpSecurity.build()).thenReturn(expectedChain);

        SecurityConfig config = new SecurityConfig();
        SecurityFilterChain resultado = config.filterChain(httpSecurity);

        assertNotNull(resultado);
        org.junit.jupiter.api.Assertions.assertEquals(expectedChain, resultado);
    }
}