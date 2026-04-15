package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
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
    @NotNull
    private Prioridad prioridad;
    @NotBlank
    @Size(min = 5, max = 500)
    private String justificacionPrioridad;
}
