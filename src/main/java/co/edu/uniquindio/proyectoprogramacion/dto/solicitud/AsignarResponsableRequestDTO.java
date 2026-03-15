package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarResponsableRequestDTO {

    @NotNull(message = "El responsableId es obligatorio")
    @Min(value = 1, message = "El responsableId debe ser mayor a 0")
    private Long responsableId;
}