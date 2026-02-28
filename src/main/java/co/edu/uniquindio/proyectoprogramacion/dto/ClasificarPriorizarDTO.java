package co.edu.uniquindio.proyectoprogramacion.dto;

import co.edu.uniquindio.proyectoprogramacion.model.enums.PrioridadSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClasificarPriorizarDTO {

    @NotNull
    private TipoSolicitud tipoSolicitud;

    @NotNull
    private PrioridadSolicitud prioridad;

    @NotBlank
    private String justificacionPrioridad;
}