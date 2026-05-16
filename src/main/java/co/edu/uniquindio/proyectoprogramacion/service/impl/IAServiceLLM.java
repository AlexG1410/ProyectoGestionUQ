package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import co.edu.uniquindio.proyectoprogramacion.service.client.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación de IAService que integra con Google Gemini API.
 * 
 * Esta implementación se activa cuando gemini.api-key está configurada.
 * 
 * Proporciona funcionalidades de IA REAL:
 * - Generación de resúmenes inteligentes de solicitudes
 * - Sugerencia de prioridades basadas en contexto y urgencia
 * - Clasificación automática de solicitudes por tipo
 * 
 * Incluye fallback robusto: si Gemini falla, devuelve valores por defecto
 * manteniendo RF-11 (funcionamiento independiente de IA).
 */
@Slf4j
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
@RequiredArgsConstructor
public class IAServiceLLM implements IAService {

    private final LLMClient llmClient;
    private final SolicitudRepository solicitudRepository;
    private final HistorialSolicitudRepository historialRepository;

    @Override
    @Transactional(readOnly = true)
    public String resumirSolicitud(UUID solicitudId, UUID usuarioId, RolUsuario rol) {
        Solicitud solicitud = solicitudRepository.findByIdWithUsuarios(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        if (RolUsuario.ESTUDIANTE.equals(rol) && !solicitud.getSolicitante().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
        }

        List<HistorialSolicitud> historial = historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId);

        try {
            String prompt = construirPromptResumen(solicitud, historial);
            String resumen = llmClient.sendPrompt(prompt);

            if (resumen == null || resumen.isBlank()) {
                throw new IllegalStateException("Gemini devolvió una respuesta vacía");
            }

            if (resumen.trim().length() < 120) {
                throw new IllegalStateException("Gemini devolvió un resumen demasiado corto o incompleto");
            }

            log.info("Resumen generado exitosamente para solicitud {}", solicitudId);
            return resumen.trim();
        } catch (Exception e) {
            log.warn("Error al generar resumen con Gemini, usando fallback: {}", e.getMessage());
            return generarResumenFallback(solicitud, historial);
        }
    }

    @Override
    public SugerirPrioridadResponseDTO sugerirPrioridad(SugerirPrioridadRequestDTO dto) {
        try {
            String prompt = construirPromptSugerirPrioridad(dto);
            String respuestaRaw = llmClient.sendPrompt(prompt);
            return parsearRespuestaSugerirPrioridad(respuestaRaw);
        } catch (Exception e) {
            log.warn("Error al sugerir prioridad con Gemini, usando fallback: {}", e.getMessage());
            return sugerenciaPrioridadFallback();
        }
    }

    @Override
    public SugerirClasificacionPrioridadResponseDTO sugerirClasificacionYPrioridad(
            SugerirClasificacionPrioridadRequestDTO dto) {
        try {
            String prompt = construirPromptClasificacionYPrioridad(dto);
            String respuestaRaw = llmClient.sendPrompt(prompt);
            return parsearRespuestaClasificacionYPrioridad(respuestaRaw);
        } catch (Exception e) {
            log.warn("Error al sugerir clasificación con Gemini, usando fallback: {}", e.getMessage());
            return sugerenciaClasificacionFallback();
        }
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null) {
            return "No registra";
        }

        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";

        String nombreCompleto = (nombres + " " + apellidos).trim();

        return nombreCompleto.isEmpty() ? usuario.getUsername() : nombreCompleto;
    }

    private String valor(Object valor) {
        return valor == null ? "No registra" : valor.toString();
    }

    // ============= CONSTRUCTORES DE PROMPTS =============

    private String construirPromptResumen(Solicitud solicitud, List<HistorialSolicitud> historial) {
        String responsable = solicitud.getResponsable() != null
                ? nombreCompleto(solicitud.getResponsable())
                : "Sin responsable asignado";

        String eventosHistorial = historial.isEmpty()
                ? "Sin eventos registrados en historial."
                : historial.stream()
                .map(h -> "- %s | Acción: %s | Actor: %s | Detalle: %s | Observaciones: %s"
                        .formatted(
                                h.getFechaHora(),
                                h.getAccion(),
                                nombreCompleto(h.getActor()),
                                valor(h.getDetalle()),
                                valor(h.getObservaciones())
                        ))
                .collect(Collectors.joining("\n"));

        return """
            Genera un resumen académico claro, completo y profesional en español.

            Información actual:
            - Tipo: %s
            - Estado actual: %s
            - Prioridad: %s
            - Impacto académico: %s
            - Canal de origen: %s
            - Fecha de registro: %s
            - Fecha límite: %s
            - Solicitante: %s
            - Responsable: %s
            - Descripción: %s

            Historial de la solicitud:
            %s

            Instrucciones:
            - Redacta entre 80 y 150 palabras.
            - No hagas una frase corta.
            - No cortes la respuesta.
            - Explica qué solicita el estudiante.
            - Menciona el estado actual de la solicitud.
            - Menciona la prioridad y el impacto académico si son relevantes.
            - Resume los eventos importantes del historial.
            - No incluyas el ID completo de la solicitud.
            - Responde solo con el resumen final, sin títulos ni listas.
            """.formatted(
                valor(solicitud.getTipoSolicitud()),
                valor(solicitud.getEstado()),
                valor(solicitud.getPrioridad()),
                valor(solicitud.getImpactoAcademico()),
                valor(solicitud.getCanalOrigen()),
                valor(solicitud.getFechaHoraRegistro()),
                valor(solicitud.getFechaLimite()),
                nombreCompleto(solicitud.getSolicitante()),
                responsable,
                valor(solicitud.getDescripcion()),
                eventosHistorial
        );
    }

    private String construirPromptSugerirPrioridad(SugerirPrioridadRequestDTO dto) {
        LocalDate hoy = LocalDate.now();
        long diasRestantes = dto.getFechaLimite() != null ? 
                ChronoUnit.DAYS.between(hoy, dto.getFechaLimite()) : 
                Long.MAX_VALUE;

        return """
                Analiza los siguientes parámetros académicos y sugiere una prioridad:
                
                - Tipo de solicitud: %s
                - Impacto académico: %s
                - Fecha límite: %s (en %d días)
                
                Debes responder EXACTAMENTE en JSON puro sin markdown:
                {
                  "prioridad": "[BAJA|MEDIA|ALTA|CRITICA]",
                  "justificacion": "Breve justificación"
                }
                
                Reglas de decisión:
                - Si CRITICO o menos de 3 días → CRITICA
                - Si ALTO o menos de 7 días → ALTA
                - Si MEDIO → MEDIA
                - Si BAJO → BAJA
                
                Responde SOLO JSON válido, sin texto adicional.
                """.formatted(
                        dto.getTipoSolicitud(),
                        dto.getImpactoAcademico(),
                        dto.getFechaLimite(),
                        diasRestantes
                );
    }

    private String construirPromptClasificacionYPrioridad(SugerirClasificacionPrioridadRequestDTO dto) {
        return """
                Analiza la siguiente solicitud académica y sugiere su clasificación y prioridad:
                
                Descripción: "%s"
                Canal origen: %s
                Impacto académico: %s
                Fecha límite: %s
                
                Debes responder EXACTAMENTE en JSON puro sin markdown:
                {
                  "tipoSolicitud": "[REGISTRO_ASIGNATURAS|HOMOLOGACION|CANCELACION_ASIGNATURAS|SOLICITUD_CUPOS|CONSULTA_ACADEMICA]",
                  "prioridad": "[BAJA|MEDIA|ALTA|CRITICA]",
                  "confianza": [0.0-1.0],
                  "justificacion": "Explicación breve de la clasificación"
                }
                
                Guías:
                - "registro" o "inscripción" → REGISTRO_ASIGNATURAS
                - "convalidar" u "homologar" → HOMOLOGACION
                - "cancelar" o "retirar" → CANCELACION_ASIGNATURAS
                - "cupo" → SOLICITUD_CUPOS
                - Otros casos → CONSULTA_ACADEMICA
                - Si ambiguo, usa baja confianza (< 0.6)
                
                Responde SOLO JSON válido, sin texto adicional.
                """.formatted(
                        dto.getDescripcion(),
                        dto.getCanalOrigen(),
                        dto.getImpactoAcademico(),
                        dto.getFechaLimite()
                );
    }

    // ============= PARSERS DE RESPUESTAS JSON =============

    private SugerirPrioridadResponseDTO parsearRespuestaSugerirPrioridad(String respuesta) {
        try {
            String json = limpiarJSON(respuesta);
            
            Prioridad prioridad = extraerPrioridadJSON(json, "prioridad");
            String justificacion = extraerTextoJSON(json, "justificacion");

            return SugerirPrioridadResponseDTO.builder()
                    .prioridadSugerida(prioridad)
                    .puntajeTotal(calcularPuntaje(prioridad))
                    .razones(List.of(justificacion))
                    .build();
        } catch (Exception e) {
            log.error("Error al parsear respuesta de prioridad: {}", e.getMessage());
            return sugerenciaPrioridadFallback();
        }
    }

    private SugerirClasificacionPrioridadResponseDTO parsearRespuestaClasificacionYPrioridad(String respuesta) {
        try {
            String json = limpiarJSON(respuesta);
            
            TipoSolicitud tipo = extraerTipoSolicitudJSON(json, "tipoSolicitud");
            Prioridad prioridad = extraerPrioridadJSON(json, "prioridad");
            Double confianza = extraerConfianzaJSON(json);
            String justificacion = extraerTextoJSON(json, "justificacion");

            return SugerirClasificacionPrioridadResponseDTO.builder()
                    .tipoSolicitudSugerido(tipo)
                    .prioridadSugerida(prioridad)
                    .confianza(confianza)
                    .puntajeTotal(calcularPuntaje(prioridad))
                    .razones(List.of(justificacion))
                    .requiereConfirmacionHumana(confianza < 0.7)
                    .build();
        } catch (Exception e) {
            log.error("Error al parsear respuesta de clasificación: {}", e.getMessage());
            return sugerenciaClasificacionFallback();
        }
    }

    // ============= EXTRACTORES DE JSON =============

    private String limpiarJSON(String texto) {
        // Remover markdown code blocks si existen
        texto = texto.replaceAll("```json\\s*", "");
        texto = texto.replaceAll("```\\s*", "");
        return texto.trim();
    }

    private Prioridad extraerPrioridadJSON(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([A-Z_]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Prioridad.valueOf(matcher.group(1).trim());
            } catch (IllegalArgumentException e) {
                log.warn("Prioridad no reconocida: {}", matcher.group(1));
            }
        }
        return Prioridad.MEDIA;
    }

    private TipoSolicitud extraerTipoSolicitudJSON(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([A-Z_]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return TipoSolicitud.valueOf(matcher.group(1).trim());
            } catch (IllegalArgumentException e) {
                log.warn("Tipo de solicitud no reconocido: {}", matcher.group(1));
            }
        }
        return TipoSolicitud.CONSULTA_ACADEMICA;
    }

    private Double extraerConfianzaJSON(String json) {
        Pattern pattern = Pattern.compile("\"confianza\"\\s*:\\s*([0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1).trim());
            } catch (NumberFormatException e) {
                log.warn("Confianza no válida: {}", matcher.group(1));
            }
        }
        return 0.0;
    }

    private String extraerTextoJSON(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Sugerencia generada por Gemini";
    }

    // ============= HELPERS =============

    private int calcularPuntaje(Prioridad prioridad) {
        return switch (prioridad) {
            case CRITICA -> 100;
            case ALTA -> 75;
            case MEDIA -> 50;
            case BAJA -> 25;
        };
    }

    // ============= FALLBACKS (RF-11: FUNCIONAMIENTO SIN IA) =============

    private String generarResumenFallback(Solicitud solicitud, List<HistorialSolicitud> historial) {
        String responsable = solicitud.getResponsable() != null
                ? nombreCompleto(solicitud.getResponsable())
                : "sin responsable asignado";

        String ultimoEvento = historial.isEmpty()
                ? "No hay eventos adicionales registrados en el historial."
                : historial.get(historial.size() - 1).getAccion() + " - " +
                valor(historial.get(historial.size() - 1).getDetalle());

        return """
            La solicitud académica fue registrada por %s y actualmente se encuentra en estado %s.
            Su tipo es %s, con prioridad %s e impacto académico %s.
            La descripción registrada es: "%s".
            El responsable actual es %s.
            Último evento del historial: %s.
            """.formatted(
                nombreCompleto(solicitud.getSolicitante()),
                valor(solicitud.getEstado()),
                valor(solicitud.getTipoSolicitud()),
                valor(solicitud.getPrioridad()),
                valor(solicitud.getImpactoAcademico()),
                valor(solicitud.getDescripcion()),
                responsable,
                ultimoEvento
        );
    }

    private SugerirPrioridadResponseDTO sugerenciaPrioridadFallback() {
        return SugerirPrioridadResponseDTO.builder()
                .prioridadSugerida(Prioridad.MEDIA)
                .puntajeTotal(50)
                .razones(List.of("Gemini API no disponible"))
                .build();
    }

    private SugerirClasificacionPrioridadResponseDTO sugerenciaClasificacionFallback() {
        return SugerirClasificacionPrioridadResponseDTO.builder()
                .tipoSolicitudSugerido(TipoSolicitud.CONSULTA_ACADEMICA)
                .prioridadSugerida(Prioridad.MEDIA)
                .confianza(0.0)
                .puntajeTotal(50)
                .razones(List.of("Gemini API no disponible"))
                .requiereConfirmacionHumana(true)
                .build();
    }
}
