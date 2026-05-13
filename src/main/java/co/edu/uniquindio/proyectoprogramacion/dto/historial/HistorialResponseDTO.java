package co.edu.uniquindio.proyectoprogramacion.dto.historial;

import co.edu.uniquindio.proyectoprogramacion.model.enums.AccionHistorial;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialResponseDTO {
    private LocalDateTime fechaHora;
    private AccionHistorial accion;
    private String usuarioResponsable;
    private String detalle;
    private String observaciones;
}
