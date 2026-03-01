package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private String type;
    private String username;
    private List<String> roles;
}