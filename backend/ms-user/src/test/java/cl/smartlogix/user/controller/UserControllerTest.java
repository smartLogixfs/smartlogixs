package cl.smartlogix.user.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import cl.smartlogix.user.dto.LoginRequest;
import cl.smartlogix.user.dto.LoginResponse;
import cl.smartlogix.user.dto.UserDto;
import cl.smartlogix.user.model.User;
import cl.smartlogix.user.service.UserService;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildUser() {
        return new User(
                1L,
                "juan@test.cl",
                "Juan Perez",
                "passwordEncriptada",
                "987654321",
                "Direccion",
                "Metropolitana",
                "Puente Alto"
        );
    }

    @Test
    void debeCrearUsuario() {
        UserDto dto = new UserDto(
                null, "Juan Perez", "juan@test.cl", "123456",
                "987654321", "Direccion", "Metropolitana", "Puente Alto"
        );
        User userGuardado = buildUser();

        when(userService.create(any(UserDto.class))).thenReturn(userGuardado);

        UserDto resultado = controller.create(dto);

        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.name());
        assertEquals("juan@test.cl", resultado.email());
        assertEquals(null, resultado.password()); // UserDto.from no expone password

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void debeListarUsuarios() {
        User user1 = buildUser();
        User user2 = new User(
                2L, "maria@test.cl", "Maria Lopez", "hash",
                "999999999", "Otra Direccion", "RM", "La Florida"
        );

        when(userService.list()).thenReturn(List.of(user1, user2));

        List<UserDto> resultado = controller.list();

        assertEquals(2, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).name());
        assertEquals("Maria Lopez", resultado.get(1).name());

        verify(userService, times(1)).list();
    }

    @Test
    void debeListarUsuariosVacio() {
        when(userService.list()).thenReturn(List.of());

        List<UserDto> resultado = controller.list();

        assertTrue(resultado.isEmpty());
        verify(userService).list();
    }

    @Test
    void debeBuscarUsuarioPorId() {
        User user = buildUser();

        when(userService.findById(1L)).thenReturn(user);

        UserDto resultado = controller.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Juan Perez", resultado.name());

        verify(userService).findById(1L);
    }

    @Test
    void debeActualizarUsuario() {
        UserDto dto = new UserDto(
                null, "Juan Actualizado", "juan@test.cl", "",
                "111111111", "Nueva Direccion", "RM", "La Florida"
        );

        User actualizado = new User(
                1L, "juan@test.cl", "Juan Actualizado", "hash",
                "111111111", "Nueva Direccion", "RM", "La Florida"
        );

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(actualizado);

        UserDto resultado = controller.update(1L, dto);

        assertEquals("Juan Actualizado", resultado.name());
        assertEquals("La Florida", resultado.district());

        verify(userService).update(eq(1L), any(UserDto.class));
    }

    @Test
    void debeEliminarUsuario() {
        controller.delete(1L);

        verify(userService, times(1)).delete(1L);
    }

    @Test
    void debeHacerLoginExitoso() {
        LoginRequest request = new LoginRequest("juan@test.cl", "123456");
        LoginResponse response = new LoginResponse(true, "Login exitoso");

        when(userService.login("juan@test.cl", "123456")).thenReturn(response);

        LoginResponse resultado = controller.login(request);

        assertTrue(resultado.success());
        assertEquals("Login exitoso", resultado.message());

        verify(userService).login("juan@test.cl", "123456");
    }

    @Test
    void debeFallarLoginConCredencialesIncorrectas() {
        LoginRequest request = new LoginRequest("juan@test.cl", "incorrecta");
        LoginResponse response = new LoginResponse(false, "Credenciales incorrectas");

        when(userService.login("juan@test.cl", "incorrecta")).thenReturn(response);

        LoginResponse resultado = controller.login(request);

        assertFalse(resultado.success());
        assertEquals("Credenciales incorrectas", resultado.message());

        verify(userService).login("juan@test.cl", "incorrecta");
    }
}