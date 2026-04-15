package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirClasificacionPrioridadRequestDTO {
    private String descripcion;
    private CanalOrigen canalOrigen;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
}
