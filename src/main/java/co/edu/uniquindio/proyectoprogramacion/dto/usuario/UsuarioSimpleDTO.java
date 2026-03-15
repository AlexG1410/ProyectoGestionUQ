package co.edu.uniquindio.proyectoprogramacion.dto.usuario;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.RolUsuario;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSimpleDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private RolUsuario rol;
    private Boolean activo;
}