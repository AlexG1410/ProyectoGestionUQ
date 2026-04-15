package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthMeResponseDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String identificacion;
    private boolean authenticated;
    private List<RolUsuario> roles;
    private boolean activo;
}