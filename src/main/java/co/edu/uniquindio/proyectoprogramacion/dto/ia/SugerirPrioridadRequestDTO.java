package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirPrioridadRequestDTO {
    @NotNull(message = "Tipo de solicitud es requerido")
    private TipoSolicitud tipoSolicitud;
    
    @NotNull(message = "Impacto académico es requerido")
    private ImpactoAcademico impactoAcademico;
    
    private LocalDate fechaLimite;
}
