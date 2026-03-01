package co.edu.uniquindio.proyectoprogramacion.dto;

import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SugerirPrioridadRequestDTO {

    @NotNull
    private TipoSolicitud tipoSolicitud;

    /**
     * Valores sugeridos: BAJO, MEDIO, ALTO, CRITICO
     * (aceptamos variaciones en el engine)
     */
    private String impactoAcademico;

    /**
     * Formato esperado: yyyy-MM-dd
     * Puede ser null
     */
    private String fechaLimite;
}