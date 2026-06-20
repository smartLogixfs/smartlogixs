package cl.smartlogix.user.service;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cl.smartlogix.user.dto.LoginResponse;
import cl.smartlogix.user.dto.UserDto;
import cl.smartlogix.user.model.User;
import cl.smartlogix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE
    public User create(UserDto dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = new User(
                null, // ID autogenerado
                dto.email(),
                dto.name(),
                encoder.encode(dto.password()),
                dto.phone(),
                dto.address(),
                dto.region(),
                dto.district()
        );

        return repository.save(user);
    }

    // READ - listar todos
    public List<User> list() {
        return repository.findAll();
    }

    // READ - buscar por ID
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // READ - buscar por email (login y validaciones)
    public User findByEmail(String email) {
        User user = repository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return user;
    }

    // UPDATE
    public User update(Long id, UserDto dto) {
        User user = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(dto.name());
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setRegion(dto.region());
        user.setDistrict(dto.district());

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(encoder.encode(dto.password()));
        }

        return repository.save(user);
    }

    // DELETE
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Usuario no existe");
        }
        repository.deleteById(id);
    }

    // LOGIN
    public LoginResponse login(String email, String password) {
        User user = findByEmail(email);

        if (!encoder.matches(password, user.getPassword())) {
            return new LoginResponse(false, "Contraseña incorrecta");
        }

        return new LoginResponse(true, "Login exitoso");
    }
}
