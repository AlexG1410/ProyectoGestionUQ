package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResponsableResumenDTO {
    private UUID id;
    private String username;
    private String nombreCompleto;
}