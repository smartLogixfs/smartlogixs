package cl.smartlogix.envio.service;

import cl.smartlogix.envio.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.envio.dto.AsignarTransportistaRequest;
import cl.smartlogix.envio.dto.CrearEnvioRequest;
import cl.smartlogix.envio.dto.ShipmentDto;
import cl.smartlogix.envio.dto.ShipmentTrackingDto;
import cl.smartlogix.envio.model.EstadoEnvio;

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
