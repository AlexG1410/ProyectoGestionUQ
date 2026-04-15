package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirClasificacionPrioridadResponseDTO {
    private TipoSolicitud tipoSolicitudSugerido;
    private Prioridad prioridadSugerida;
    private Double confianza;
    private Integer puntajeTotal;
    private List<String> razones;
    private boolean requiereConfirmacionHumana;
}
