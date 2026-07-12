package cl.smartlogix.order.factory;

import cl.smartlogix.order.model.OrderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Selecciona el {@link OrderFactory} concreto segun el tipo de pedido. Spring
 * inyecta todos los creadores disponibles; agregar un tipo nuevo solo requiere
 * un nuevo {@code @Component}, sin tocar el servicio (principio Open/Closed).
 */
@Component
public class OrderFactoryProvider {

    private final Map<OrderType, OrderFactory> factories;

    public OrderFactoryProvider(List<OrderFactory> available) {
        this.factories = available.stream()
            .collect(Collectors.toMap(OrderFactory::type, Function.identity()));
    }

    public OrderFactory forType(OrderType type) {
        OrderType key = (type != null) ? type : OrderType.ESTANDAR;
        OrderFactory factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Tipo de pedido no soportado: " + key);
        }
        return factory;
    }
}
