package cl.smartlogix.shipping;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

class ShipmentApplicationTests {

    @Test
    void mainMethodExists() {

        assertDoesNotThrow(() -> {
            Class.forName("cl.smartlogix.shipping.ShipmentApplication");
        });

    }
}