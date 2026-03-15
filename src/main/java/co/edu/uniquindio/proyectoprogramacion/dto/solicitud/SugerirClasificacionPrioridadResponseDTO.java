package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.TipoSolicitud;
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
    private Integer puntajeTotal;
    private List<String> razones;
    private boolean requiereConfirmacionHumana;
}