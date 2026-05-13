package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import co.edu.uniquindio.proyectoprogramacion.service.impl.IAServiceNoop;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para servicios de IA con Google Gemini.
 * 
 * Proporciona:
 * - WebClient para comunicación HTTP con Gemini API
 * - GeminiProperties para inyección de configuración
 */
@Configuration
public class GeminiConfiguration {

    /**
     * Configura WebClient para llamadas a Gemini API.
     * Solo se crea si gemini.api-key está configurada.
     */
    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
    public WebClient webClient(GeminiProperties geminiProperties) {
        return WebClient.builder()
                .baseUrl(geminiProperties.getBaseUrl())
                .build();
    }

    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
    public IAService iaServiceNoop(SolicitudRepository solicitudRepository) {
        return new IAServiceNoop(solicitudRepository);
    }
}
