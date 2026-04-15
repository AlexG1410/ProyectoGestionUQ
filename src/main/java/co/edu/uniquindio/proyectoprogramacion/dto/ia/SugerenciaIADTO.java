package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerenciaIADTO {
    private UUID id;
    private TipoSolicitud tipoSolicitudSugerido;
    private Prioridad prioridadSugerida;
    private String resumenSugerido;
    private Double confianza;
    private Integer puntajeTotal;
    private boolean requiereConfirmacionHumana;
    private boolean confirmada;
    private LocalDateTime generadaEn;
}
