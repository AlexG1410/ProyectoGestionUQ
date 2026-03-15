package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.Prioridad;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriorizarSolicitudRequestDTO {

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;

    @NotBlank(message = "La justificación de prioridad es obligatoria")
    @Size(min = 5, max = 500, message = "La justificación debe tener entre 5 y 500 caracteres")
    private String justificacionPrioridad;
}