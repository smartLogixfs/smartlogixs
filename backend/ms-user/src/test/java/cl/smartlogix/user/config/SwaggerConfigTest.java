package cl.smartlogix.user.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class SwaggerConfigTest {

    @Test
    void debeCrearOpenAPIConDatosCorrectos() {
        SwaggerConfig config = new SwaggerConfig();

        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getContact());

        org.junit.jupiter.api.Assertions.assertEquals(
                "API User SmartLogix", openAPI.getInfo().getTitle());
        org.junit.jupiter.api.Assertions.assertEquals(
                "1.0.0", openAPI.getInfo().getVersion());
        org.junit.jupiter.api.Assertions.assertEquals(
                "soporte@smartlogix.cl", openAPI.getInfo().getContact().getEmail());
        org.junit.jupiter.api.Assertions.assertEquals(
                "Equipo Backend", openAPI.getInfo().getContact().getName());
    }
}