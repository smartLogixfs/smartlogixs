package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.shipping.dto.AsignarTransportistaRequest;
import cl.smartlogix.shipping.dto.CrearEnvioRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.EstadoEnvio;

import java.util.List;

public interface ShipmentService {
    ShipmentDto crear(CrearEnvioRequest req);
    ShipmentDto findById(Long id);
    ShipmentDto findByTracking(String trackingNumber);
    List<ShipmentDto> findByPedido(Long idPedido);
    List<ShipmentDto> findAll();
    List<ShipmentDto> findByEstado(EstadoEnvio estado);
    List<ShipmentTrackingDto> historial(Long idEnvio);

    ShipmentDto asignarTransportista(Long idEnvio, AsignarTransportistaRequest req);
    ShipmentDto cambiarEstado(Long idEnvio, ActualizarEstadoEnvioRequest req);
}
