package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;

@Component
public class HistorialMapper {

    public HistorialResponseDTO toResponse(HistorialSolicitud historial) {
        return HistorialResponseDTO.builder()
                .fechaHora(historial.getFechaHora())
                .accion(historial.getAccion())
                .usuarioResponsable(historial.getActor().getUsername())
                .observaciones(historial.getObservaciones())
                .build();
    }
}