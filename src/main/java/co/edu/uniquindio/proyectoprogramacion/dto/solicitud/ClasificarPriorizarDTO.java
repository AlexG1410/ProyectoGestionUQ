package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificarPriorizarDTO {
    @NotNull
    private TipoSolicitud tipoSolicitud;
    
    /**
     * Prioridad - OPCIONAL. Si no se proporciona, se calcula automáticamente por reglas de negocio.
     * Si se proporciona, se usa como valor explícito (override).
     */
    private Prioridad prioridad;
    
    /**
     * Justificación de prioridad - OPCIONAL. Si no se proporciona, se calcula automáticamente por reglas.
     * Si se proporciona, se usa como valor explícito.
     */
    @Size(min = 5, max = 500, message = "La justificación debe tener entre 5 y 500 caracteres")
    private String justificacionPrioridad;
}
