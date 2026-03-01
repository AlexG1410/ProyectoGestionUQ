package co.edu.uniquindio.proyectoprogramacion.mappers;

import co.edu.uniquindio.proyectoprogramacion.dto.SolicitudResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.SolicitudAcademica;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    public SolicitudResponseDTO toResponse(SolicitudAcademica s) {
        return SolicitudResponseDTO.builder()
                .id(s.getId())
                .tipoSolicitud(s.getTipoSolicitud() != null ? s.getTipoSolicitud().name() : null)
                .descripcion(s.getDescripcion())
                .canalOrigen(s.getCanalOrigen() != null ? s.getCanalOrigen().name() : null)
                .fechaHoraRegistro(s.getFechaHoraRegistro())
                .identificacionSolicitante(s.getIdentificacionSolicitante())
                .estado(s.getEstado() != null ? s.getEstado().name() : null)
                .prioridad(s.getPrioridad() != null ? s.getPrioridad().name() : null)
                .justificacionPrioridad(s.getJustificacionPrioridad())
                .responsable(s.getResponsableAsignado() != null ? s.getResponsableAsignado().getUsername() : null)
                .cerrada(s.isCerrada())
                .build();
    }
}