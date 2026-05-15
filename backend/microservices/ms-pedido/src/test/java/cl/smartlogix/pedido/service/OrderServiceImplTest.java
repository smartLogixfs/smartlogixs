package cl.smartlogix.pedido.service;

import cl.smartlogix.pedido.dto.ActualizarEstadoRequest;
import cl.smartlogix.pedido.dto.CrearPedidoRequest;
import cl.smartlogix.pedido.dto.OrderDto;
import cl.smartlogix.pedido.model.EstadoPedido;
import cl.smartlogix.pedido.model.Pedido;
import cl.smartlogix.pedido.model.TipoPedido;
import cl.smartlogix.pedido.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock OrderRepository repository;
    @InjectMocks OrderServiceImpl service;

    @Test
    void crearCalculaTotalesConIVA19YDejaEnPendiente() {
        var req = new CrearPedidoRequest(
            TipoPedido.ESTANDAR,
            "CL-001",
            null,
            List.of(
                new CrearPedidoRequest.ItemRequest(1L, "SKU-001", 2, new BigDecimal("5000")),
                new CrearPedidoRequest.ItemRequest(2L, "SKU-002", 1, new BigDecimal("3000"))
            )
        );
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        OrderDto result = service.crear(req);

        assertThat(result.subtotal()).isEqualByComparingTo("13000.00");
        assertThat(result.impuesto()).isEqualByComparingTo("2470.00");
        assertThat(result.total()).isEqualByComparingTo("15470.00");
        assertThat(result.estado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(result.items()).hasSize(2);
        assertThat(result.codigo()).startsWith("PED-").hasSize(19); // PED- (4) + yyyyMMdd (8) + - (1) + 6 = 19
    }

    @Test
    void crearSinItemsLanzaBadRequest() {
        var req = new CrearPedidoRequest(TipoPedido.ESTANDAR, "CL-001", null, List.of());

        assertThatThrownBy(() -> service.crear(req))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("al menos un ítem");
    }

    @Test
    void crearSinTipoUsaEstandarPorDefecto() {
        var req = new CrearPedidoRequest(
            null, "CL-001", null,
            List.of(new CrearPedidoRequest.ItemRequest(1L, "SKU-001", 1, new BigDecimal("1000")))
        );
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        OrderDto result = service.crear(req);

        assertThat(result.tipo()).isEqualTo(TipoPedido.ESTANDAR);
    }

    @Test
    void cambiarEstadoTransicionPermitidaActualiza() {
        Pedido pedido = pedidoEnEstado(EstadoPedido.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        OrderDto result = service.cambiarEstado(1L, new ActualizarEstadoRequest(EstadoPedido.APROBADO, "Pago OK"));

        assertThat(result.estado()).isEqualTo(EstadoPedido.APROBADO);
    }

    @Test
    void cambiarEstadoTransicionNoPermitidaLanzaConflict() {
        Pedido pedido = pedidoEnEstado(EstadoPedido.ENTREGADO);
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.cambiarEstado(1L,
            new ActualizarEstadoRequest(EstadoPedido.PENDIENTE, "intento ilegal")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Transición no permitida");
    }

    @Test
    void cambiarEstadoDesdeEntregadoNoEsAlNada() {
        Pedido pedido = pedidoEnEstado(EstadoPedido.ENTREGADO);
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));

        for (EstadoPedido target : EstadoPedido.values()) {
            if (target == EstadoPedido.ENTREGADO) continue;
            assertThatThrownBy(() -> service.cambiarEstado(1L, new ActualizarEstadoRequest(target, null)))
                .as("ENTREGADO no puede ir a %s", target)
                .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Test
    void findByIdInexistenteLanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Pedido no encontrado");
    }

    private Pedido pedidoEnEstado(EstadoPedido estado) {
        return Pedido.builder()
            .idPedido(1L)
            .codigo("PED-20260513-AB12CD")
            .tipo(TipoPedido.ESTANDAR)
            .estado(estado)
            .idCliente("CL-001")
            .subtotal(BigDecimal.ZERO)
            .impuesto(BigDecimal.ZERO)
            .total(BigDecimal.ZERO)
            .build();
    }
}
