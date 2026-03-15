package co.edu.uniquindio.proyectoprogramacion.dto.historial;

import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleRefDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialResponseDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private AccionHistorial accion;
    private UsuarioSimpleRefDTO usuarioResponsable;
    private String observaciones;
    private EstadoSolicitud estadoAnterior;
    private EstadoSolicitud estadoNuevo;
    private Prioridad prioridadAnterior;
    private Prioridad prioridadNueva;
}