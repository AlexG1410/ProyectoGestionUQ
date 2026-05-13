package co.edu.uniquindio.proyectoprogramacion.service.client;

import co.edu.uniquindio.proyectoprogramacion.config.GeminiProperties;
import co.edu.uniquindio.proyectoprogramacion.util.gemini.GeminiRequest;
import co.edu.uniquindio.proyectoprogramacion.util.gemini.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Cliente para comunicación con Google Gemini API.
 * Encapsula toda la lógica de HTTP, parsing y manejo de errores.
 */
@Slf4j
@Component
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
@RequiredArgsConstructor
public class LLMClient {

    private final GeminiProperties geminiProperties;
    private final WebClient webClient;

    /**
     * Envía un prompt a Gemini y recibe la respuesta de texto.
     * 
     * @param prompt El texto del prompt a enviar
     * @return Texto de respuesta de Gemini
     * @throws RuntimeException Si hay error en la llamada o respuesta inválida
     */
    public String sendPrompt(String prompt) {
        try {
            log.debug("Enviando prompt a Gemini: {} caracteres", prompt.length());

            GeminiRequest request = construirRequest(prompt);
            
            GeminiResponse response = webClient
                    .post()
                    .uri(construirUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofMillis(geminiProperties.getTimeoutMs()))
                    .block();

            if (response == null) {
                log.error("Respuesta nula de Gemini API");
                throw new RuntimeException("Respuesta nula de Gemini API");
            }

            String responseText = extraerTextoRespuesta(response);
            log.debug("Respuesta recibida de Gemini: {} caracteres", responseText.length());
            
            return responseText;

        } catch (WebClientResponseException e) {
            log.error("Error HTTP desde Gemini API: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error en Gemini API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al comunicarse con Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Error al conectar con Gemini API: " + e.getMessage());
        }
    }

    // ============= MÉTODOS PRIVADOS =============

    private GeminiRequest construirRequest(String prompt) {
        return GeminiRequest.builder()
                .contents(java.util.List.of(
                        GeminiRequest.Content.builder()
                                .parts(java.util.List.of(
                                        GeminiRequest.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .maxOutputTokens(geminiProperties.getMaxTokens())
                        .temperature(geminiProperties.getTemperature())
                        .build())
                .build();
    }

    private String construirUri() {
        return String.format("%s/v1beta/models/%s:generateContent?key=%s",
                geminiProperties.getBaseUrl(),
                geminiProperties.getModel(),
                geminiProperties.getApiKey());
    }

    private String extraerTextoRespuesta(GeminiResponse response) {
        if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Gemini retornó lista de candidatos vacía");
        }

        GeminiResponse.Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null 
                || candidate.getContent().getParts().isEmpty()) {
            throw new RuntimeException("Gemini retornó contenido vacío");
        }

        String texto = candidate.getContent().getParts().get(0).getText();
        if (texto == null || texto.trim().isEmpty()) {
            throw new RuntimeException("Gemini retornó texto vacío");
        }

        return texto.trim();
    }
}

