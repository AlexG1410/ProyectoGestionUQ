package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CerrarSolicitudDTO {

    @NotBlank(message = "La observación de cierre es obligatoria")
    @Size(min = 5, max = 1000, message = "La observación de cierre debe tener entre 5 y 1000 caracteres")
    private String observacionCierre;
}