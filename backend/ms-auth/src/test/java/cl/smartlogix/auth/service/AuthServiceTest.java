package cl.smartlogix.auth.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.smartlogix.auth.domain.UserAccount;
import cl.smartlogix.auth.dto.AuthResponse;
import cl.smartlogix.auth.dto.LoginRequest;
import cl.smartlogix.auth.dto.RegisterRequest;
import cl.smartlogix.auth.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService service;

    @Test
    void registerShouldSaveUser() {

        RegisterRequest request =
                new RegisterRequest(
                        "Juan Perez",
                        "juan@test.cl",
                        "Password123"
                );

        when(repository.existsByEmailIgnoreCase("juan@test.cl"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("HASH");

        service.register(request);

        verify(repository).save(any(UserAccount.class));
    }

    @Test
    void registerShouldThrowWhenEmailExists() {

        RegisterRequest request =
                new RegisterRequest(
                        "Juan Perez",
                        "juan@test.cl",
                        "Password123"
                );

        when(repository.existsByEmailIgnoreCase("juan@test.cl"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void loginShouldReturnToken() {

        UserAccount user = new UserAccount();
        user.setEmail("juan@test.cl");
        user.setName("Juan");
        user.setRole("USER");
        user.setEnabled(true);
        user.setPasswordHash("HASH");

        when(repository.findByEmailIgnoreCase("juan@test.cl"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Password123", "HASH"))
                .thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("TOKEN");

        when(jwtService.getAccessTokenTtlSeconds())
                .thenReturn(1800L);

        AuthResponse response =
                service.login(
                        new LoginRequest(
                                "juan@test.cl",
                                "Password123"
                        )
                );

        assertEquals("TOKEN", response.accessToken());
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {

        when(repository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> service.login(
                        new LoginRequest(
                                "test@test.cl",
                                "Password123"
                        )
                )
        );
    }

    @Test
    void loginShouldThrowWhenPasswordInvalid() {

        UserAccount user = new UserAccount();
        user.setEnabled(true);
        user.setPasswordHash("HASH");

        when(repository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> service.login(
                        new LoginRequest(
                                "test@test.cl",
                                "Password123"
                        )
                )
        );
    }

    @Test
void loginShouldThrowWhenUserDisabled() {

    UserAccount user = new UserAccount();
    user.setEnabled(false);
    user.setPasswordHash("HASH");

    when(repository.findByEmailIgnoreCase(anyString()))
            .thenReturn(Optional.of(user));

    assertThrows(
            BadCredentialsException.class,
            () -> service.login(
                    new LoginRequest(
                            "test@test.cl",
                            "Password123"
                    )
            )
    );
}
}