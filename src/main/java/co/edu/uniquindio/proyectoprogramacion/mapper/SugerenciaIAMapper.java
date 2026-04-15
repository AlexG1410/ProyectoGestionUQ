package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerenciaIADTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.SugerenciaIA;
import org.springframework.stereotype.Component;

@Component
public class SugerenciaIAMapper {

    public SugerenciaIADTO toDto(SugerenciaIA sugerencia) {
        return SugerenciaIADTO.builder()
                .id(sugerencia.getId())
                .tipoSolicitudSugerido(sugerencia.getTipoSolicitudSugerido())
                .prioridadSugerida(sugerencia.getPrioridadSugerida())
                .resumenSugerido(sugerencia.getResumenSugerido())
                .confianza(sugerencia.getConfianza())
                .puntajeTotal(sugerencia.getPuntajeTotal())
                .requiereConfirmacionHumana(sugerencia.isRequiereConfirmacionHumana())
                .confirmada(sugerencia.isConfirmada())
                .generadaEn(sugerencia.getGeneradaEn())
                .build();
    }
}
