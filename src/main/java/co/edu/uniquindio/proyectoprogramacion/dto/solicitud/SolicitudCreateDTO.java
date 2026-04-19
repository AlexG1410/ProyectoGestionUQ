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
     * Tipo de solicitud - OPCIONAL en registro.
     * El estudiante registra la solicitud sin conocer su tipo académico exacto.
     * El tipo se define después en el proceso de CLASIFICACIÓN (RF-02).
     */
    private TipoSolicitud tipoSolicitud;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El canal de origen es obligatorio")
    private CanalOrigen canalOrigen;
    
    private ImpactoAcademico impactoAcademico;
    
    private LocalDate fechaLimite;
}