package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleRefDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDTO {
    private Long id;
    private TipoSolicitud tipoSolicitud;
    private String descripcion;
    private CanalOrigen canalOrigen;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
    private LocalDateTime fechaHoraRegistro;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private String justificacionPrioridad;
    private UsuarioSimpleRefDTO solicitante;
    private UsuarioSimpleRefDTO responsable;
    private Boolean cerrada;
    private LocalDateTime fechaCierre;
}