package co.edu.uniquindio.proyectoprogramacion.dto.usuario;

import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSimpleDTO {
    private UUID id;
    private String username;
    private String nombreCompleto;
    private RolUsuario rol;
    private Boolean activo;
}