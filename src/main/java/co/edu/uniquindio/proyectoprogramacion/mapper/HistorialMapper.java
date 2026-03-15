package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialMapper {

    private final UsuarioMapper usuarioMapper;

    public HistorialResponseDTO toHistorialResponseDTO(HistorialSolicitud historial) {
        if (historial == null) return null;

        return HistorialResponseDTO.builder()
                .id(historial.getId())
                .fechaHora(historial.getFechaHora())
                .accion(historial.getAccion())
                .usuarioResponsable(usuarioMapper.toUsuarioSimpleRefDTO(historial.getUsuarioResponsable()))
                .observaciones(historial.getObservaciones())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .prioridadAnterior(historial.getPrioridadAnterior())
                .prioridadNueva(historial.getPrioridadNueva())
                .build();
    }
}