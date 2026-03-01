package co.edu.uniquindio.proyectoprogramacion.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SugerirPrioridadResponseDTO {
    private String prioridadSugerida;
    private Integer puntajeTotal;
    private List<String> razones;
}