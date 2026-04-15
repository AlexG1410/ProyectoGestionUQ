package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarcarAtendidoDTO {
    @NotBlank
    @Size(min = 3, max = 500)
    private String observacion;
}