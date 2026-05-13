package co.edu.uniquindio.proyectoprogramacion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para Google Gemini API.
 * Se mapean desde application.properties con prefijo "gemini".
 */
@Component
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiProperties {
    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private String model = "gemini-1.5-flash";
    private int maxTokens = 512;
    private long timeoutMs = 10000;
    private float temperature = 0.7f;
}
