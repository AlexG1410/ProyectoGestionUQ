package co.edu.uniquindio.proyectoprogramacion.mappers;

import co.edu.uniquindio.proyectoprogramacion.dto.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.HistorialSolicitud;
import org.springframework.stereotype.Component;

@Component
public class HistorialMapper {

    public HistorialResponseDTO toResponse(HistorialSolicitud h) {
        return HistorialResponseDTO.builder()
                .fechaHora(h.getFechaHora())
                .accion(h.getAccion())
                .usuarioResponsable(h.getUsuarioResponsable())
                .observaciones(h.getObservaciones())
                .build();
    }
}