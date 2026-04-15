package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolicitudMapper {

    private final UsuarioMapper usuarioMapper;

    public SolicitudResponseDTO toResponse(Solicitud solicitud) {
        return SolicitudResponseDTO.builder()
                .id(solicitud.getId())
                .tipoSolicitud(solicitud.getTipoSolicitud())
                .descripcion(solicitud.getDescripcion())
                .canalOrigen(solicitud.getCanalOrigen())
                .impactoAcademico(solicitud.getImpactoAcademico())
                .fechaHoraRegistro(solicitud.getFechaHoraRegistro())
                .identificacionSolicitante(solicitud.getSolicitante().getIdentificacion())
                .estado(solicitud.getEstado())
                .prioridad(solicitud.getPrioridad())
                .justificacionPrioridad(solicitud.getJustificacionPrioridad())
                .responsable(usuarioMapper.toResponsableResumen(solicitud.getResponsable()))
                .fechaCierre(solicitud.getFechaCierre())
                .observacionCierre(solicitud.getObservacionCierre())
                .build();
    }
}