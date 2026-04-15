package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FiltroSolicitudesDTO {
    private EstadoSolicitud estado;
    private TipoSolicitud tipo;
    private Prioridad prioridad;
    private UUID responsableId;
}
