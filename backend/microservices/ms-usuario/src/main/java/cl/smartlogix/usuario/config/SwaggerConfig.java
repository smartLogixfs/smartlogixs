package cl.smartlogix.usuario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Usuario SmartLogix")
                .version("1.0.0")
                .description("Documentación de la API desarrollada en Spring Boot para SmartLogix")
                .contact(new Contact()
                    .name("Equipo Backend")
                    .email("soporte@smartlogix.cl"))
            );
    }
}
