package cl.smartlogix.auth.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class JwtKeyConfigTest {

    private final JwtKeyConfig config =
            new JwtKeyConfig();

    @Test
    void privateKeyShouldThrowExceptionForInvalidFile() {

        assertThrows(
                IllegalStateException.class,
                () -> config.privateKey(
                        "src/test/resources/test-private.pem"
                )
        );
    }

    @Test
    void publicKeyShouldThrowExceptionForInvalidFile() {

        assertThrows(
                IllegalStateException.class,
                () -> config.publicKey(
                        "src/test/resources/test-public.pem"
                )
        );
    }
}