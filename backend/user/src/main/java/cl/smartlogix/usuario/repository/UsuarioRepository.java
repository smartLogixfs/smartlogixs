package cl.smartlogix.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.smartlogix.usuario.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    Usuario findByCorreo(String correo);
}
