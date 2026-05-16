package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.service.client.LLMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests avanzados de resiliencia para IAServiceLLM.
 *
 * Validaciones:
 * - Manejo de múltiples tipos de errores
 * - Comportamiento bajo estrés
 * - Validación de rangos de valores
 * - Fallback en diferentes escenarios
 *
 * Estos tests aseguran que RF-11 se cumple: el sistema funciona sin IA.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IAServiceLLM Resilience Tests")
class IAServiceLLMResilienceTest {

    @Mock
    private LLMClient llmClient;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private HistorialSolicitudRepository historialRepository;

    private IAServiceLLM iaService;

    @BeforeEach
    void setUp() {
        iaService = new IAServiceLLM(llmClient, solicitudRepository, historialRepository);
    }

    // ============= TESTS DE MANEJO DE EXCEPCIONES =============

    @Test
    @DisplayName("Debe manejar TimeoutException correctamente")
    void testManejandoTimeout_RetornaFallback() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("java.util.concurrent.TimeoutException: Timeout after 10000 ms"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback en timeout");
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida(), "Fallback es MEDIA");
        assertTrue(resultado.getRazones().stream()
                        .anyMatch(r -> r.toLowerCase().contains("disponible")),
                "Razones deben indicar que IA no está disponible");
    }

    @Test
    @DisplayName("Debe manejar NetworkException")
    void testManejandoNetworkError_RetornaFallback() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("java.net.ConnectException: Connection refused"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback en error de red");
        assertNotNull(resultado.getPrioridadSugerida(), "Prioridad no debe ser nula");
    }

    @Test
    @DisplayName("Debe manejar API Key inválida (Error 401)")
    void testManejandoUnauthorized_RetornaFallback() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("HTTP 401: Unauthorized - Invalid API key"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback en error 401");
    }

    @Test
    @DisplayName("Debe manejar cuota excedida (Error 429)")
    void testManejandoRateLimited_RetornaFallback() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("HTTP 429: Too Many Requests - Rate limit exceeded"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback en rate limit");
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida());
    }

    @Test
    @DisplayName("Debe manejar error 500 del servidor")
    void testManejandoServerError_RetornaFallback() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("HTTP 500: Internal Server Error"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback en error 500");
    }

    // ============= TESTS DE VALIDACIÓN DE VALORES =============

    @ParameterizedTest
    @EnumSource(Prioridad.class)
    @DisplayName("Puntaje debe ser correcto para cada prioridad")
    void testPuntaje_DebeSerCorrectoPorPrioridad(Prioridad prioridad) {
        String json = String.format(
                "{\"prioridad\": \"%s\", \"justificacion\": \"Test\"}",
                prioridad.name()
        );

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        int puntajeEsperado = switch (prioridad) {
            case CRITICA -> 100;
            case ALTA -> 75;
            case MEDIA -> 50;
            case BAJA -> 25;
        };

        assertEquals(puntajeEsperado, resultado.getPuntajeTotal(),
                "Puntaje debe ser " + puntajeEsperado + " para " + prioridad);
    }

    @ParameterizedTest
    @EnumSource(TipoSolicitud.class)
    @DisplayName("Debe parsear todos los tipos de solicitud correctamente")
    void testTipoSolicitud_DebeParsearlaTodos(TipoSolicitud tipo) {
        String json = String.format(
                "{\"tipoSolicitud\": \"%s\", \"prioridad\": \"MEDIA\", \"confianza\": 0.8, \"justificacion\": \"Test\"}",
                tipo.name()
        );

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirClasificacionPrioridadRequestDTO dto = crearClasificacionDTO();

        SugerirClasificacionPrioridadResponseDTO resultado =
                iaService.sugerirClasificacionYPrioridad(dto);

        assertEquals(tipo, resultado.getTipoSolicitudSugerido(),
                "Debe parsear tipo " + tipo.name());
    }

    @Test
    @DisplayName("Confianza debe estar siempre entre 0.0 y 1.0")
    void testConfianza_DebeEstarEnRangoValido() {
        String[] respuestas = {
                "{\"tipoSolicitud\": \"HOMOLOGACION\", \"prioridad\": \"MEDIA\", \"confianza\": 0.0, \"justificacion\": \"Test\"}",
                "{\"tipoSolicitud\": \"HOMOLOGACION\", \"prioridad\": \"MEDIA\", \"confianza\": 0.5, \"justificacion\": \"Test\"}",
                "{\"tipoSolicitud\": \"HOMOLOGACION\", \"prioridad\": \"MEDIA\", \"confianza\": 1.0, \"justificacion\": \"Test\"}"
        };

        SugerirClasificacionPrioridadRequestDTO dto = crearClasificacionDTO();

        for (String respuesta : respuestas) {
            when(llmClient.sendPrompt(anyString())).thenReturn(respuesta);

            SugerirClasificacionPrioridadResponseDTO resultado =
                    iaService.sugerirClasificacionYPrioridad(dto);

            assertTrue(resultado.getConfianza() >= 0.0 && resultado.getConfianza() <= 1.0,
                    "Confianza debe estar entre 0.0 y 1.0, pero fue: " + resultado.getConfianza());
        }
    }

    // ============= TESTS DE CAMPOS FALTANTES =============

    @Test
    @DisplayName("Debe usar valores por defecto si falta campo prioridad en JSON")
    void testCampoFaltante_Prioridad_UsaDefault() {
        String json = "{\"justificacion\": \"Test sin prioridad\"}";

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado.getPrioridadSugerida(), "Debe tener prioridad default");
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida(),
                "Default debe ser MEDIA si falta campo");
    }

    @Test
    @DisplayName("Debe usar valores por defecto si falta campo tipoSolicitud")
    void testCampoFaltante_TipoSolicitud_UsaDefault() {
        String json = "{\"prioridad\": \"ALTA\", \"confianza\": 0.8, \"justificacion\": \"Sin tipo\"}";

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirClasificacionPrioridadRequestDTO dto = crearClasificacionDTO();

        SugerirClasificacionPrioridadResponseDTO resultado =
                iaService.sugerirClasificacionYPrioridad(dto);

        assertEquals(TipoSolicitud.CONSULTA_ACADEMICA, resultado.getTipoSolicitudSugerido(),
                "Default debe ser CONSULTA_ACADEMICA si falta tipo");
    }

    // ============= TESTS DE RESPUESTA VACÍA O NULA =============

    @Test
    @DisplayName("Debe manejar respuesta vacía de LLMClient")
    void testRespuestaVacia_RetornaFallback() {
        when(llmClient.sendPrompt(anyString())).thenReturn("");

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback para respuesta vacía");
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida());
    }

    @Test
    @DisplayName("Debe manejar espacios en blanco solamente")
    void testRespuestaBlanca_RetornaFallback() {
        when(llmClient.sendPrompt(anyString())).thenReturn("   \n\t  ");

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertNotNull(resultado, "Debe retornar fallback para espacios en blanco");
    }

    // ============= TESTS DE ROBUSTEZ =============

    @Test
    @DisplayName("Debe manejar JSON con caracteres especiales")
    void testJSONConCaracteresEspeciales_ParseaCorrectamente() {
        String json = "{\"prioridad\": \"ALTA\", \"justificacion\": \"Contiene: ñ, é, á, ó, ú\"}";

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida(),
                "Debe parsear JSON con caracteres especiales");
    }

    @Test
    @DisplayName("Debe manejar JSON con espacios extras")
    void testJSONConEspacios_ParseaCorrectamente() {
        String json = "{  \"prioridad\"  :  \"MEDIA\"  ,  \"justificacion\"  :  \"Test\"  }";

        when(llmClient.sendPrompt(anyString())).thenReturn(json);

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida(),
                "Debe parsear JSON con espacios extras");
    }

    @Test
    @DisplayName("Debe ser thread-safe (múltiples llamadas concurrentes)")
    void testThreadSafety_MultiplesLlamadas() throws InterruptedException {
        when(llmClient.sendPrompt(anyString()))
                .thenReturn("{\"prioridad\": \"ALTA\", \"justificacion\": \"Test\"}");

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);
                assertNotNull(resultado, "Resultado no debe ser nulo");
                assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida());
            });
        }

        executorService.shutdown();

        boolean termino = executorService.awaitTermination(3, TimeUnit.SECONDS);

        assertTrue(termino, "Las llamadas concurrentes deben terminar correctamente");
    }

    // ============= TESTS DE CONSISTENCIA =============

    @Test
    @DisplayName("Misma entrada debe producir mismo fallback")
    void testConsistencia_FallbackDeterminista() {
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Error"));

        SugerirPrioridadRequestDTO dto = crearSugerirPrioridadDTO();

        SugerirPrioridadResponseDTO[] resultados = new SugerirPrioridadResponseDTO[5];

        for (int i = 0; i < 5; i++) {
            resultados[i] = iaService.sugerirPrioridad(dto);
        }

        for (int i = 1; i < 5; i++) {
            assertEquals(resultados[0].getPrioridadSugerida(), resultados[i].getPrioridadSugerida(),
                    "Fallback debe ser determinista");

            assertEquals(resultados[0].getPuntajeTotal(), resultados[i].getPuntajeTotal());
        }
    }

    // ============= HELPER METHODS =============

    private SugerirPrioridadRequestDTO crearSugerirPrioridadDTO() {
        return SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .fechaLimite(LocalDate.now().plusDays(10))
                .build();
    }

    private SugerirClasificacionPrioridadRequestDTO crearClasificacionDTO() {
        return SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Test description for classification")
                .canalOrigen(CanalOrigen.CORREO)
                .impactoAcademico(ImpactoAcademico.MEDIO)
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();
    }
}