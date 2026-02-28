package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistorialResponseDTO {
    private LocalDateTime fechaHora;
    private String accion;
    private String usuarioResponsable;
    private String observaciones;
}