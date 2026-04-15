package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirPrioridadRequestDTO {
    private TipoSolicitud tipoSolicitud;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
}
