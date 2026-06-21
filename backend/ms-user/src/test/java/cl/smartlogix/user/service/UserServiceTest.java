package cl.smartlogix.user.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import cl.smartlogix.user.dto.LoginResponse;
import cl.smartlogix.user.dto.UserDto;
import cl.smartlogix.user.model.User;
import cl.smartlogix.user.repository.UserRepository;

class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void debeCrearUsuarioCorrectamente() {

        UserDto dto = new UserDto(
                null,
                "Juan Perez",
                "juan@test.cl",
                "123456",
                "987654321",
                "Direccion",
                "Metropolitana",
                "Puente Alto"
        );

        User userGuardado = new User(
                1L,
                "juan@test.cl",
                "Juan Perez",
                "passwordEncriptada",
                "987654321",
                "Direccion",
                "Metropolitana",
                "Puente Alto"
        );

        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(userGuardado);

        User resultado = service.create(dto);

        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getName());

        verify(repository).save(any(User.class));
    }

    @Test
    void noDebeCrearUsuarioConCorreoDuplicado() {

        UserDto dto = new UserDto(
                null,
                "Juan",
                "juan@test.cl",
                "123456",
                "987654321",
                "Direccion",
                "RM",
                "Puente Alto"
        );

        when(repository.existsByEmail(dto.email())).thenReturn(true);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> service.create(dto));

        assertEquals("El correo ya está registrado",
                exception.getMessage());
    }

    @Test
    void debeBuscarUsuarioPorId() {

        User user = new User(
                1L,
                "juan@test.cl",
                "Juan",
                "123",
                "987654321",
                "Direccion",
                "RM",
                "Puente Alto"
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        User resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getName());
    }

    @Test
    void debeLanzarErrorSiUsuarioNoExistePorId() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> service.findById(1L));

        assertEquals("Usuario no encontrado",
                exception.getMessage());
    }

    @Test
    void debeBuscarUsuarioPorEmail() {

        User user = new User(
                1L,
                "juan@test.cl",
                "Juan",
                "123",
                "987654321",
                "Direccion",
                "RM",
                "Puente Alto"
        );

        when(repository.findByEmail("juan@test.cl"))
                .thenReturn(user);

        User resultado = service.findByEmail("juan@test.cl");

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getName());
    }

    @Test
    void debeLanzarErrorSiEmailNoExiste() {

        when(repository.findByEmail("juan@test.cl"))
                .thenReturn(null);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> service.findByEmail("juan@test.cl"));

        assertEquals("Usuario no encontrado",
                exception.getMessage());
    }

    @Test
    void debeEliminarUsuario() {

        when(repository.existsById(1L))
                .thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void noDebeEliminarUsuarioInexistente() {

        when(repository.existsById(1L))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> service.delete(1L));

        assertEquals("Usuario no existe",
                exception.getMessage());
    }

    @Test
    void debeActualizarUsuario() {

        User user = new User(
                1L,
                "juan@test.cl",
                "Juan",
                "123",
                "987654321",
                "Direccion",
                "RM",
                "Puente Alto"
        );

        UserDto dto = new UserDto(
                null,
                "Juan Actualizado",
                "juan@test.cl",
                "",
                "111111111",
                "Nueva Direccion",
                "RM",
                "La Florida"
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.save(any(User.class)))
                .thenReturn(user);

        User resultado = service.update(1L, dto);

        assertEquals("Juan Actualizado",
                resultado.getName());
    }

    @Test
void debeListarUsuarios() {

    User user = new User(
            1L,
            "juan@test.cl",
            "Juan",
            "123",
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    when(repository.findAll()).thenReturn(List.of(user));

    List<User> resultado = service.list();

    assertEquals(1, resultado.size());
    assertEquals("Juan", resultado.get(0).getName());

    verify(repository).findAll();
}

@Test
void debeLanzarErrorAlActualizarUsuarioInexistente() {

    UserDto dto = new UserDto(
            null,
            "Juan",
            "juan@test.cl",
            "",
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    when(repository.findById(99L))
            .thenReturn(Optional.empty());

    RuntimeException exception =
            assertThrows(RuntimeException.class,
                    () -> service.update(99L, dto));

    assertEquals(
            "Usuario no encontrado",
            exception.getMessage()
    );
}

@Test
void debeActualizarPasswordCuandoSeEnviaNuevaPassword() {

    User user = new User(
            1L,
            "juan@test.cl",
            "Juan",
            "123",
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    UserDto dto = new UserDto(
            null,
            "Juan",
            "juan@test.cl",
            "nuevaPassword",
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    when(repository.findById(1L))
            .thenReturn(Optional.of(user));

    when(repository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    User resultado = service.update(1L, dto);

    assertNotNull(resultado.getPassword());

    verify(repository).save(any(User.class));
}

@Test
void debeRealizarLoginExitoso() {

    BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    User user = new User(
            1L,
            "juan@test.cl",
            "Juan",
            encoder.encode("123456"),
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    when(repository.findByEmail("juan@test.cl"))
            .thenReturn(user);

    LoginResponse response =
            service.login(
                    "juan@test.cl",
                    "123456"
            );

    assertTrue(response.success());
    assertEquals(
            "Login exitoso",
            response.message()
    );
}

@Test
void debeFallarLoginPorPasswordIncorrecta() {

    BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    User user = new User(
            1L,
            "juan@test.cl",
            "Juan",
            encoder.encode("123456"),
            "987654321",
            "Direccion",
            "RM",
            "Puente Alto"
    );

    when(repository.findByEmail("juan@test.cl"))
            .thenReturn(user);

    LoginResponse response =
            service.login(
                    "juan@test.cl",
                    "passwordMala"
            );

    assertFalse(response.success());
    assertEquals(
            "Contraseña incorrecta",
            response.message()
    );
}

}