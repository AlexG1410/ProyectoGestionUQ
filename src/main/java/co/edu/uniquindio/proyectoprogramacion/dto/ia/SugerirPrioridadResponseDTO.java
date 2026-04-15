package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirPrioridadResponseDTO {
    private Prioridad prioridadSugerida;
    private Integer puntajeTotal;
    private List<String> razones;
}
