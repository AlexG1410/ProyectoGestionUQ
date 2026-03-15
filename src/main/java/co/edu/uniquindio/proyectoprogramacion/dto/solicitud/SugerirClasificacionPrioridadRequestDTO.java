package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.ImpactoAcademico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SugerirClasificacionPrioridadRequestDTO {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    private CanalOrigen canalOrigen;
    private ImpactoAcademico impactoAcademico;
    private LocalDate fechaLimite;
}