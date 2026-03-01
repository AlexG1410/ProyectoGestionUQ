package co.edu.uniquindio.proyectoprogramacion.dto;

import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudCreateDTO {

    @NotNull
    private TipoSolicitud tipoSolicitud;

    @NotBlank
    @Size(max = 2000)
    private String descripcion;

    @NotNull
    private CanalOrigen canalOrigen;

    @NotBlank
    private String identificacionSolicitante;

    // opcionales para priorización por reglas
    private String impactoAcademico;
    private String fechaLimite; // ISO yyyy-MM-dd (para simplificar request)
}