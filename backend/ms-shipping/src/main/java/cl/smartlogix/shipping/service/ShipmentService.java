package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.UpdateShipmentRStatusRequest;
import cl.smartlogix.shipping.dto.AssingCarierRequest;
import cl.smartlogix.shipping.dto.CreateShipmentRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.ShipmentState;

import java.util.List;

public interface ShipmentService {
    ShipmentDto crear(CreateShipmentRequest req);
    ShipmentDto findById(Long id);
    ShipmentDto findByTracking(String trackingNumber);
    List<ShipmentDto> findByPedido(Long idPedido);
    List<ShipmentDto> findAll();
    List<ShipmentDto> findByEstado(ShipmentState estado);
    List<ShipmentTrackingDto> historial(Long idEnvio);

    ShipmentDto asignarTransportista(Long idEnvio, AssingCarierRequest req);
    ShipmentDto cambiarEstado(Long idEnvio, UpdateShipmentRStatusRequest req);
}
