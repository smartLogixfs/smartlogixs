package cl.smartlogix.usuario.service;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import cl.smartlogix.usuario.dto.LoginResponse;
import cl.smartlogix.usuario.dto.UsuarioDto;
import cl.smartlogix.usuario.model.Usuario;
import cl.smartlogix.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE
    public Usuario registrar(UsuarioDto dto) {
        if (repository.existsByCorreo(dto.correo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario(
                null, // ID autogenerado
                dto.correo(),
                dto.nombre(),
                encoder.encode(dto.password()),
                dto.telefono(),
                dto.direccion(),
                dto.region(),
                dto.comuna()
        );

        return repository.save(usuario);
    }

    // READ - listar todos
    public List<Usuario> listar() {
        return repository.findAll();
    }

    // READ - buscar por ID
    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // READ - buscar por correo (para login y validaciones)
    public Usuario buscarPorCorreo(String correo) {
        Usuario usuario = repository.findByCorreo(correo);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return usuario;
    }

    // UPDATE
    public Usuario actualizar(Long id, UsuarioDto dto) {
        Usuario usuario = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(dto.nombre());
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setRegion(dto.region());
        usuario.setComuna(dto.comuna());

        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPassword(encoder.encode(dto.password()));
        }

        return repository.save(usuario);
    }

    // DELETE
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Usuario no existe");
        }
        repository.deleteById(id);
    }

    // LOGIN
    public LoginResponse login(String correo, String password) {
        Usuario usuario = buscarPorCorreo(correo);

        if (!encoder.matches(password, usuario.getPassword())) {
            return new LoginResponse(false, "Contraseña incorrecta");
        }

        return new LoginResponse(true, "Login exitoso");
    }
}
