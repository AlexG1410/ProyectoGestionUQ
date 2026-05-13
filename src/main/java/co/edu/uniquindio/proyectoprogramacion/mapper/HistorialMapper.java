package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import org.springframework.stereotype.Component;

@Component
public class HistorialMapper {

    public HistorialResponseDTO toResponse(HistorialSolicitud historial) {
        return HistorialResponseDTO.builder()
                .fechaHora(historial.getFechaHora())
                .accion(historial.getAccion())
                .usuarioResponsable(historial.getActor().getUsername())
                .detalle(historial.getDetalle())
                .observaciones(historial.getObservaciones())
                .build();
    }
}
