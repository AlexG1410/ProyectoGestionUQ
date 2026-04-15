package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudCreateDTO {
    @NotNull
    private TipoSolicitud tipoSolicitud;
    @NotBlank
    @Size(min = 10, max = 2000)
    private String descripcion;
    @NotNull
    private CanalOrigen canalOrigen;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
}