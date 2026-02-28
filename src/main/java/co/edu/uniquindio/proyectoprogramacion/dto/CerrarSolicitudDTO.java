package co.edu.uniquindio.proyectoprogramacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CerrarSolicitudDTO {
    @NotBlank
    private String observacionCierre;
}