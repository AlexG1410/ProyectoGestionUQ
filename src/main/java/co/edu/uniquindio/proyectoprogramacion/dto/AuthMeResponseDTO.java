package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthMeResponseDTO {
    private String username;
    private boolean authenticated;
    private List<String> roles;
}