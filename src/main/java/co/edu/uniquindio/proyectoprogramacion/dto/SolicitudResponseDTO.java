package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudResponseDTO {
    private Long id;
    private String tipoSolicitud;
    private String descripcion;
    private String canalOrigen;
    private LocalDateTime fechaHoraRegistro;
    private String identificacionSolicitante;
    private String estado;
    private String prioridad;
    private String justificacionPrioridad;
    private String responsable;
    private Boolean cerrada;
}