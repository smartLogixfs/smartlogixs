package cl.smartlogix.shipping.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    @Test
    void shouldCreateOpenApiDefinition() {

        OpenApiConfig config = new OpenApiConfig();

        OpenAPI api = config.customOpenAPI();

        assertNotNull(api);
        assertEquals("ms-shipping — API SmartLogix", api.getInfo().getTitle());
        assertEquals("1.0.0", api.getInfo().getVersion());
    }
}