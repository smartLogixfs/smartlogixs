package cl.smartlogix.usuario.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.smartlogix.usuario.dto.LoginDto;
import cl.smartlogix.usuario.dto.LoginResponse;
import cl.smartlogix.usuario.dto.UsuarioDto;
import cl.smartlogix.usuario.model.Usuario;
import cl.smartlogix.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // CREATE
    @Operation(
        summary = "Registrar usuario",
        description = "Crea un nuevo usuario en el sistema."
    )
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente")
    @PostMapping
    public Usuario registrar(@RequestBody UsuarioDto usuarioDto) {
        return usuarioService.registrar(usuarioDto);
    }

    // READ - listar todos
    @Operation(
        summary = "Listar usuarios",
        description = "Devuelve una lista con todos los usuarios registrados."
    )
    @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    // READ - por ID
    @Operation(
        summary = "Buscar usuario por ID",
        description = "Devuelve un usuario específico según su ID."
    )
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}")
    public Usuario buscar(
        @Parameter(description = "ID del usuario a buscar") 
        @PathVariable Long id
    ) {
        return usuarioService.buscar(id);
    }

    // UPDATE - por ID
    @Operation(
        summary = "Actualizar usuario",
        description = "Modifica los datos de un usuario según su ID."
    )
    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente")
    @PutMapping("/{id}")
    public Usuario actualizar(
        @Parameter(description = "ID del usuario a actualizar")
        @PathVariable Long id,
        @RequestBody UsuarioDto usuarioDto
    ) {
        return usuarioService.actualizar(id, usuarioDto);
    }

    // DELETE - por ID
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema."
    )
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @DeleteMapping("/{id}")
    public void eliminar(
        @Parameter(description = "ID del usuario a eliminar")
        @PathVariable Long id
    ) {
        usuarioService.eliminar(id);
    }

    // LOGIN
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida las credenciales del usuario y retorna sus datos."
    )
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginDto loginDto) {
        return usuarioService.login(loginDto.correo(), loginDto.password());
    }
}
