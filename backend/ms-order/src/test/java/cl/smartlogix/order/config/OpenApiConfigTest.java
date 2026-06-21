package cl.smartlogix.order.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    @Test
    void customOpenAPIShouldReturnConfiguredObject() {

        OpenApiConfig config =
                new OpenApiConfig();

        OpenAPI openAPI =
                config.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());

        assertEquals(
                "ms-order — API SmartLogix",
                openAPI.getInfo().getTitle()
        );

        assertEquals(
                "1.0.0",
                openAPI.getInfo().getVersion()
        );

        assertNotNull(
                openAPI.getInfo().getContact()
        );

        assertEquals(
                "Equipo Backend SmartLogix",
                openAPI.getInfo().getContact().getName()
        );

        assertEquals(
                "soporte@smartlogix.cl",
                openAPI.getInfo().getContact().getEmail()
        );

        assertNotNull(
                openAPI.getInfo().getLicense()
        );

        assertEquals(
                "MIT",
                openAPI.getInfo().getLicense().getName()
        );
    }
}