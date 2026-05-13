package co.edu.uniquindio.proyectoprogramacion.dto.ia;

import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerirClasificacionPrioridadRequestDTO {
    @NotBlank(message = "Descripción es requerida")
    @Size(min = 10, max = 2000, message = "La descripcion debe tener entre 10 y 2000 caracteres")
    private String descripcion;
    
    @NotNull(message = "Canal origen es requerido")
    private CanalOrigen canalOrigen;
    
    @NotNull(message = "Impacto académico es requerido")
    private ImpactoAcademico impactoAcademico;
    
    private LocalDate fechaLimite;
}
