package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarAtencionDTO {
    @Size(max = 500, message = "La observacion no puede superar 500 caracteres")
    private String observacion;
}
