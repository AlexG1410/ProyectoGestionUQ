package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String type;
    private String username;
    private List<String> roles;
}