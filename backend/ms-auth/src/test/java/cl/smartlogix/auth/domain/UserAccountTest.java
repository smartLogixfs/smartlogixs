package cl.smartlogix.auth.domain;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void gettersAndSettersShouldWork() {

        Instant now = Instant.now();

        UserAccount user = new UserAccount();

        user.setName("Juan");
        user.setEmail("juan@test.cl");
        user.setPasswordHash("HASH");
        user.setRole("ADMIN");
        user.setEnabled(true);
        user.setCreatedAt(now);

        assertEquals("Juan", user.getName());
        assertEquals("juan@test.cl", user.getEmail());
        assertEquals("HASH", user.getPasswordHash());
        assertEquals("ADMIN", user.getRole());
        assertTrue(user.getEnabled());
        assertEquals(now, user.getCreatedAt());
    }
}