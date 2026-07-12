package cl.smartlogix.user.controller;

import java.time.OffsetDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de demostracion para la Letra G (registro de errores y logs con GlitchTip).
 * Permiten provocar eventos en cada capa y verlos en el dashboard. Solo para demo.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("service", "ms-user", "status", "ok", "timestamp", OffsetDateTime.now().toString());
    }

    @GetMapping("/log")
    public Map<String, String> log() {
        // Evento de log nivel ERROR: lo captura el appender sentry-logback -> GlitchTip.
        log.error("[DEMO] Evento de log de prueba enviado a GlitchTip desde ms-user");
        return Map.of("sent", "ERROR log enviado a GlitchTip");
    }

    @GetMapping("/error")
    public Map<String, String> error() {
        // Excepcion no controlada: la captura el GlobalExceptionHandler (log.error) -> GlitchTip.
        throw new RuntimeException("[DEMO] Excepcion de prueba para GlitchTip desde ms-user");
    }
}
