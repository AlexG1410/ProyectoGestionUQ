package co.edu.uniquindio.proyectoprogramacion.dto.usuario;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSimpleRefDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
}