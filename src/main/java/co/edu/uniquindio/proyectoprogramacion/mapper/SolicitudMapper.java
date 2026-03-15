package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolicitudMapper {

    private final UsuarioMapper usuarioMapper;

    public SolicitudResponseDTO toSolicitudResponseDTO(Solicitud solicitud) {
        if (solicitud == null) return null;

        return SolicitudResponseDTO.builder()
                .id(solicitud.getId())
                .tipoSolicitud(solicitud.getTipoSolicitud())
                .descripcion(solicitud.getDescripcion())
                .canalOrigen(solicitud.getCanalOrigen())
                .impactoAcademico(solicitud.getImpactoAcademico())
                .fechaLimite(solicitud.getFechaLimite())
                .fechaHoraRegistro(solicitud.getFechaHoraRegistro())
                .estado(solicitud.getEstado())
                .prioridad(solicitud.getPrioridad())
                .justificacionPrioridad(solicitud.getJustificacionPrioridad())
                .solicitante(usuarioMapper.toUsuarioSimpleRefDTO(solicitud.getSolicitante()))
                .responsable(usuarioMapper.toUsuarioSimpleRefDTO(solicitud.getResponsable()))
                .cerrada(solicitud.getCerrada())
                .fechaCierre(solicitud.getFechaCierre())
                .build();
    }
}