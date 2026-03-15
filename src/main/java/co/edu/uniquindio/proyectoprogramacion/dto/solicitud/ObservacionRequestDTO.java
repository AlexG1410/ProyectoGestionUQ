package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObservacionRequestDTO {

    @Size(max = 500, message = "La observación no puede superar 500 caracteres")
    private String observacion;
}