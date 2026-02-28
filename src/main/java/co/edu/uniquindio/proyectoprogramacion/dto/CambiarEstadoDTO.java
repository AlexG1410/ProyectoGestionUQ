package co.edu.uniquindio.proyectoprogramacion.dto;

import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoDTO {
    @NotNull
    private EstadoSolicitud nuevoEstado;
    private String observacion;
}