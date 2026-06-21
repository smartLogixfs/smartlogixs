package cl.smartlogix.auth.controller;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.jwk.RSAKey;

class JwksControllerTest {

    @Test
    void jwksShouldReturnKeys() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair pair =
                generator.generateKeyPair();

        RSAKey rsaKey =
                new RSAKey.Builder(
                        (RSAPublicKey) pair.getPublic()
                ).build();

        JwksController controller =
                new JwksController(rsaKey);

        Map<String, Object> result =
                controller.jwks();

        assertNotNull(result);

        assertTrue(result.containsKey("keys"));
    }
}