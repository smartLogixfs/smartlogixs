package cl.smartlogix.shipping.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

import cl.smartlogix.shipping.dto.CreateCarrierRequest;
import cl.smartlogix.shipping.model.Carrier;
import cl.smartlogix.shipping.repository.CarrierRepository;

class CarrierServiceImplTest {

    private CarrierRepository repository;
    private CarrierServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(CarrierRepository.class);
        service = new CarrierServiceImpl(repository);
    }

    @Test
    void shouldCreateCarrier() {

        CreateCarrierRequest request =
                new CreateCarrierRequest(
                        "Chilexpress",
                        "11111111-1",
                        "999999999"
                );

        Carrier carrier = Carrier.builder()
                .id(1L)
                .name("Chilexpress")
                .rut("11111111-1")
                .contactPhone("999999999")
                .active(true)
                .build();

        when(repository.findByRut(any()))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(carrier);

        var result = service.create(request);

        assertEquals(1L, result.carrierId());
        verify(repository).save(any());
    }

    @Test
    void shouldThrowWhenRutExists() {

        when(repository.findByRut("111"))
                .thenReturn(Optional.of(new Carrier()));

        assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        new CreateCarrierRequest(
                                "Carrier",
                                "111",
                                "123"
                        )
                )
        );
    }

    @Test
    void shouldReturnActiveCarriers() {

        when(repository.findByActiveTrue())
                .thenReturn(List.of(
                        Carrier.builder()
                                .id(1L)
                                .name("Carrier")
                                .active(true)
                                .build()
                ));

        assertEquals(
                1,
                service.findActive().size()
        );
    }
}