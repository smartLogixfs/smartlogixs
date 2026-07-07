package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.CarrierDto;
import cl.smartlogix.shipping.dto.CreateCarrierRequest;
import cl.smartlogix.shipping.service.CarrierService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarrierControllerTest {

    @Test
    void createShouldReturnCreatedResponse() {

        CarrierService service = mock(CarrierService.class);

        CarrierController controller =
                new CarrierController(service);

        CreateCarrierRequest request =
                new CreateCarrierRequest(
                        "Starken",
                        "11111111-1",
                        "987654321"
                );

        CarrierDto dto =
                new CarrierDto(
                        1L,
                        "Starken",
                        "11111111-1",
                        "987654321",
                        true
                );

        when(service.create(request))
                .thenReturn(dto);

        ResponseEntity<CarrierDto> response =
                controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().carrierId());

        verify(service).create(request);
    }

    @Test
    void getByIdShouldReturnCarrier() {

        CarrierService service = mock(CarrierService.class);

        CarrierController controller =
                new CarrierController(service);

        CarrierDto dto =
                new CarrierDto(
                        1L,
                        "Starken",
                        null,
                        null,
                        true
                );

        when(service.findById(1L))
                .thenReturn(dto);

        ResponseEntity<CarrierDto> response =
                controller.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().carrierId());

        verify(service).findById(1L);
    }

    @Test
    void listShouldReturnAll() {

        CarrierService service = mock(CarrierService.class);

        CarrierController controller =
                new CarrierController(service);

        when(service.findAll())
                .thenReturn(List.of());

        ResponseEntity<List<CarrierDto>> response =
                controller.list(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(service).findAll();
    }

    @Test
    void listShouldReturnOnlyActive() {

        CarrierService service = mock(CarrierService.class);

        CarrierController controller =
                new CarrierController(service);

        when(service.findActive())
                .thenReturn(List.of());

        ResponseEntity<List<CarrierDto>> response =
                controller.list(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(service).findActive();
    }
}