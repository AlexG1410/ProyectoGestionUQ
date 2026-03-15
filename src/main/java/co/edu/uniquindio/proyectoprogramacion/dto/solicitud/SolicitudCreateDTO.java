package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.TipoSolicitud;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SolicitudCreateDTO {

    private TipoSolicitud tipoSolicitud;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El canal de origen es obligatorio")
    private CanalOrigen canalOrigen;

    private ImpactoAcademico impactoAcademico;

    private LocalDate fechaLimite;
}