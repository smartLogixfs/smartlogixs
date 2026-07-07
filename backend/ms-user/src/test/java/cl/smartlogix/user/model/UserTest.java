package cl.smartlogix.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void debeCrearUsuarioConConstructorCompleto() {

        User user = new User(
                1L,
                "juan@test.cl",
                "Juan Perez",
                "password",
                "987654321",
                "Direccion",
                "RM",
                "Puente Alto"
        );

        assertEquals(1L, user.getId());
        assertEquals("juan@test.cl", user.getEmail());
        assertEquals("Juan Perez", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals("987654321", user.getPhone());
        assertEquals("Direccion", user.getAddress());
        assertEquals("RM", user.getRegion());
        assertEquals("Puente Alto", user.getDistrict());
    }

    @Test
    void debePermitirUsoDeSettersYGetters() {

        User user = new User();

        user.setId(1L);
        user.setEmail("test@test.cl");
        user.setName("Test");
        user.setPassword("123");
        user.setPhone("999999999");
        user.setAddress("Direccion");
        user.setRegion("RM");
        user.setDistrict("La Florida");

        assertEquals(1L, user.getId());
        assertEquals("test@test.cl", user.getEmail());
        assertEquals("Test", user.getName());
        assertEquals("123", user.getPassword());
        assertEquals("999999999", user.getPhone());
        assertEquals("Direccion", user.getAddress());
        assertEquals("RM", user.getRegion());
        assertEquals("La Florida", user.getDistrict());
    }

    @Test
    void debeCrearUsuarioNoNulo() {

        User user = new User();

        assertNotNull(user);
    }
}