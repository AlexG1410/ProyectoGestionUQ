package co.edu.uniquindio.proyectoprogramacion.dto.solicitud;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsignarResponsableDTO {
    @NotNull
    private UUID responsableId;
}