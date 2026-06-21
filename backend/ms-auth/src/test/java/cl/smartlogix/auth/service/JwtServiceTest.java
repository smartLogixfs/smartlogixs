package cl.smartlogix.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import cl.smartlogix.auth.domain.UserAccount;

class JwtServiceTest {

    @Test
    void generateAccessTokenForUserRole() {

        JwtEncoder encoder =
                mock(JwtEncoder.class);

        Jwt jwt =
                mock(Jwt.class);

        when(jwt.getTokenValue())
                .thenReturn("TOKEN");

        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        JwtService service =
                new JwtService(
                        encoder,
                        "smartlogix",
                        30
                );

        UserAccount user =
                new UserAccount();

        user.setEmail("user@test.cl");
        user.setName("User");
        user.setRole("USER");

        String token =
                service.generateAccessToken(user);

        assertEquals("TOKEN", token);
    }

    @Test
    void generateAccessTokenForAdminRole() {

        JwtEncoder encoder =
                mock(JwtEncoder.class);

        Jwt jwt =
                mock(Jwt.class);

        when(jwt.getTokenValue())
                .thenReturn("ADMIN_TOKEN");

        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        JwtService service =
                new JwtService(
                        encoder,
                        "smartlogix",
                        30
                );

        UserAccount user =
                new UserAccount();

        user.setEmail("admin@test.cl");
        user.setName("Admin");
        user.setRole("ADMIN");

        String token =
                service.generateAccessToken(user);

        assertEquals("ADMIN_TOKEN", token);
    }

    @Test
    void getAccessTokenTtlSecondsShouldReturnSeconds() {

        JwtService service =
                new JwtService(
                        mock(JwtEncoder.class),
                        "smartlogix",
                        30
                );

        assertEquals(
                1800,
                service.getAccessTokenTtlSeconds()
        );
    }
}