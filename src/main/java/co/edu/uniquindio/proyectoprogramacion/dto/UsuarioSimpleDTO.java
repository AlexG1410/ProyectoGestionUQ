package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioSimpleDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String rol;
    private boolean activo;
}