package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudResponseDTO {
    private UUID id;
    private UUID solicitanteId;
    private TipoSolicitud tipoSolicitud;
    private String descripcion;
    private CanalOrigen canalOrigen;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
    private LocalDateTime fechaHoraRegistro;
    private String identificacionSolicitante;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private String justificacionPrioridad;
    private ResponsableResumenDTO responsable;
    private LocalDateTime fechaCierre;
    private String observacionCierre;
}
