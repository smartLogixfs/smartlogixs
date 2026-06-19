package cl.smartlogix.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ms-inventory — API SmartLogix")
                .version("1.0.0")
                .description("Microservicio de Inventario: productos, bodegas, stock y movimientos con optimistic locking.")
                .contact(new Contact()
                    .name("Equipo Backend SmartLogix")
                    .email("soporte@smartlogix.cl"))
                .license(new License().name("MIT")));
    }
}
