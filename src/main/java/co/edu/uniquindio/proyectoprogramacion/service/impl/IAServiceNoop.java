package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementación noop (no-operation) de IAService para desarrollo/testing.
 * Esta implementación devuelve valores por defecto sin llamar a ningún LLM.
 * 
 * En producción, se puede reemplazar con IAServiceLLM que integre con APIs de IA.
 */
@Service
@RequiredArgsConstructor
public class IAServiceNoop implements IAService {

    /**
     * Genera un resumen básico de la solicitud.
     * Actualmente devuelve un mensaje genérico (sin IA real).
     */
    @Override
    public String resumirSolicitud(UUID solicitudId) {
        return "Resumen de solicitud [" + solicitudId + "]: " +
                "Solicitud académica procesada. Para más detalles, consulte los eventos del historial.";
    }

    /**
     * Sugiere una prioridad basada en el impacto académico y fecha límite.
     * Actualmente devuelve MEDIA como sugerencia por defecto.
     */
    @Override
    public SugerirPrioridadResponseDTO sugerirPrioridad(SugerirPrioridadRequestDTO dto) {
        return SugerirPrioridadResponseDTO.builder()
                .prioridadSugerida(Prioridad.MEDIA)
                .puntajeTotal(50)
                .razones(List.of(
                        "Análisis de IA no disponible en modo noop",
                        "Se asigna prioridad MEDIA por defecto"
                ))
                .build();
    }

    /**
     * Sugiere clasificación y prioridad de solicitud.
     * Actualmente devuelve valores por defecto sin análisis real.
     */
    @Override
    public SugerirClasificacionPrioridadResponseDTO sugerirClasificacionYPrioridad(
            SugerirClasificacionPrioridadRequestDTO dto) {
        return SugerirClasificacionPrioridadResponseDTO.builder()
                .tipoSolicitudSugerido(TipoSolicitud.CONSULTA_ACADEMICA)
                .prioridadSugerida(Prioridad.MEDIA)
                .confianza(0.5)
                .puntajeTotal(50)
                .razones(List.of(
                        "Análisis de IA no disponible en modo noop",
                        "Se sugiere tipo CONSULTA_ACADEMICA y prioridad MEDIA por defecto"
                ))
                .requiereConfirmacionHumana(true)
                .build();
    }
}
