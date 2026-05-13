package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequestDTO {
    @NotBlank(message = "El token es obligatorio")
    private String token;
}
