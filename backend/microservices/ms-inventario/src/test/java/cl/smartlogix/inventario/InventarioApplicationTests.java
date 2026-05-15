package cl.smartlogix.inventario;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Necesita DB Postgres; reactivar con Testcontainers o perfil de integración")
@SpringBootTest
class InventarioApplicationTests {

	@Test
	void contextLoads() {
	}

}
