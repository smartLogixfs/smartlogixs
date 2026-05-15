package cl.smartlogix.pedido;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Smoke test que levanta el contexto Spring completo (requiere Postgres + Flyway).
// Deshabilitado en la build normal — habilitar con Testcontainers o un perfil "integration".
@Disabled("Necesita DB Postgres; reactivar con Testcontainers o perfil de integración")
@SpringBootTest
class PedidoApplicationTests {

	@Test
	void contextLoads() {
	}

}
