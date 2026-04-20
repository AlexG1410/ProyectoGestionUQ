package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudCreateDTO {
    /**
     * Tipo de solicitud - OBLIGATORIO en registro (RF-01).
     * Debe ser especificado al registrar la solicitud.
     * Valores: REGISTRO_ASIGNATURAS, HOMOLOGACION, CANCELACION_ASIGNATURAS, SOLICITUD_CUPOS, CONSULTA_ACADEMICA, OTRO.
     */
    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipoSolicitud;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El canal de origen es obligatorio")
    private CanalOrigen canalOrigen;
    
    private ImpactoAcademico impactoAcademico;
    
    private LocalDate fechaLimite;
    
    /**
     * Identificación del solicitante real (RF-01).
     * - Si es ESTUDIANTE: debe coincidir con su propia identificación
     * - Si es ADMINISTRATIVO/COORDINADOR: puede registrar para otra persona usando su identificación
     */
    @NotBlank(message = "La identificación del solicitante es obligatoria")
    @Size(min = 1, max = 50, message = "La identificación debe tener entre 1 y 50 caracteres")
    private String identificacionSolicitante;
}