package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDTO {
    private String token;
    private String type;
    private Long expiresIn;
}
