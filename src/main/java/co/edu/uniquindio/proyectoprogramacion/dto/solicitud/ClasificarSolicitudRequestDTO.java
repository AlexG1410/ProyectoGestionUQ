package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClasificarSolicitudRequestDTO {

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipoSolicitud;

    @Size(max = 500, message = "La observación no puede superar 500 caracteres")
    private String observacion;
}