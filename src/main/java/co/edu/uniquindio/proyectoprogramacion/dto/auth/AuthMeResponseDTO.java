package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthMeResponseDTO {
    private UUID id;
    private String username;
    private String nombreCompleto;
    private String identificacion;
    private String email;
    private boolean authenticated;
    private List<RolUsuario> roles;
    private boolean activo;
}