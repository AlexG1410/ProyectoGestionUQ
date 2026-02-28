package co.edu.uniquindio.proyectoprogramacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarResponsableDTO {
    @NotNull
    private Long responsableId;
}