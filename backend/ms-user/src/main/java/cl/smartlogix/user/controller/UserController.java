package cl.smartlogix.user.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.smartlogix.user.dto.LoginRequest;
import cl.smartlogix.user.dto.LoginResponse;
import cl.smartlogix.user.dto.UserDto;
import cl.smartlogix.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operaciones relacionadas con la gestión de usuarios")
public class UserController {

    private final UserService userService;

    // CREATE
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema.")
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente")
    @PostMapping
    public UserDto create(@RequestBody UserDto dto) {
        return UserDto.from(userService.create(dto));
    }

    // READ - listar
    @Operation(summary = "Listar usuarios", description = "Devuelve una lista con todos los usuarios registrados.")
    @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente")
    @GetMapping
    public List<UserDto> list() {
        return userService.list().stream().map(UserDto::from).toList();
    }

    // READ - por ID
    @Operation(summary = "Buscar usuario por ID", description = "Devuelve un usuario específico según su ID.")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}")
    public UserDto findById(
        @Parameter(description = "ID del usuario a buscar")
        @PathVariable Long id
    ) {
        return UserDto.from(userService.findById(id));
    }

    // UPDATE
    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario según su ID.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente")
    @PutMapping("/{id}")
    public UserDto update(
        @Parameter(description = "ID del usuario a actualizar")
        @PathVariable Long id,
        @RequestBody UserDto dto
    ) {
        return UserDto.from(userService.update(id, dto));
    }

    // DELETE
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema.")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @DeleteMapping("/{id}")
    public void delete(
        @Parameter(description = "ID del usuario a eliminar")
        @PathVariable Long id
    ) {
        userService.delete(id);
    }

    // LOGIN
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales del usuario y retorna su estado.")
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.email(), loginRequest.password());
    }
}
