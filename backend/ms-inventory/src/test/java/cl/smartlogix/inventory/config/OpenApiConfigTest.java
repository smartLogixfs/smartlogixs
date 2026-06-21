package cl.smartlogix.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void debeCrearOpenApiCorrectamente() {

        OpenApiConfig config = new OpenApiConfig();

        OpenAPI api = config.customOpenAPI();

        assertNotNull(api);

        assertNotNull(api.getInfo());

        assertEquals(
                "ms-inventory — API SmartLogix",
                api.getInfo().getTitle()
        );

        assertEquals(
                "1.0.0",
                api.getInfo().getVersion()
        );

        assertEquals(
                "Equipo Backend SmartLogix",
                api.getInfo().getContact().getName()
        );

        assertEquals(
                "soporte@smartlogix.cl",
                api.getInfo().getContact().getEmail()
        );

        assertEquals(
                "MIT",
                api.getInfo().getLicense().getName()
        );
    }
}