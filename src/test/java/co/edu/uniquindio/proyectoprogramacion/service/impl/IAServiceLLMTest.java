package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.service.client.LLMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para IAServiceLLM con Google Gemini API.
 * 
 * Validaciones principales:
 * - Funcionamiento básico del servicio
 * - Parsing correcto de respuestas JSON
 * - Fallback cuando Gemini falla
 * - Manejo de errores
 * 
 * NOTA: Estos tests mockean LLMClient, NO llaman a Gemini real
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IAServiceLLM Tests (Gemini Integration)")
class IAServiceLLMTest {

    @Mock
    private LLMClient llmClient;

    @Mock
    private SolicitudRepository solicitudRepository;

    private IAServiceLLM iaService;
    private UUID solicitudId;
    private UUID usuarioId;
    private Solicitud solicitudTest;
    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        iaService = new IAServiceLLM(llmClient, solicitudRepository);
        
        solicitudId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        
        // Crear usuario de prueba
        usuarioTest = new Usuario();
        usuarioTest.setId(usuarioId);
        usuarioTest.setUsername("estudiante@test.com");
        usuarioTest.setNombres("Juan");
        usuarioTest.setApellidos("Pérez");
        usuarioTest.setRol(RolUsuario.ESTUDIANTE);
        usuarioTest.setActivo(true);
        
        // Crear solicitud de prueba
        solicitudTest = new Solicitud();
        solicitudTest.setId(solicitudId);
        solicitudTest.setDescripcion("Solicitud de homologación de asignaturas");
        solicitudTest.setSolicitante(usuarioTest);
        solicitudTest.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        solicitudTest.setEstado(EstadoSolicitud.REGISTRADA);
        solicitudTest.setPrioridad(Prioridad.ALTA);
        solicitudTest.setCanalOrigen(CanalOrigen.CORREO);
        solicitudTest.setFechaHoraRegistro(LocalDateTime.now());
    }

    // ============= TESTS DE RESUMEN =============

    @Test
    @DisplayName("resumirSolicitud debe retornar resumen no nulo cuando LLMClient responde correctamente")
    void testResumirSolicitud_Success_RetornaResumenValido() {
        // Arrange
        String resumenEsperado = "Este es un resumen de la solicitud de homologación";
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudTest));
        when(llmClient.sendPrompt(anyString())).thenReturn(resumenEsperado);

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE);

        // Assert
        assertNotNull(resumen, "El resumen no debe ser nulo");
        assertEquals(resumenEsperado, resumen, "El resumen debe coincidir con la respuesta de Gemini");
        verify(llmClient, times(1)).sendPrompt(anyString());
    }

    @Test
    @DisplayName("resumirSolicitud debe retornar fallback cuando LLMClient falla")
    void testResumirSolicitud_LLMClientFails_RetornaFallback() {
        // Arrange
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudTest));
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Timeout connecting to Gemini"));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE);

        // Assert
        assertNotNull(resumen, "El fallback debe retornar un resumen válido");
        assertTrue(resumen.contains(solicitudId.toString()), "El resumen debe incluir el ID de la solicitud");
        assertTrue(resumen.contains("HOMOLOGACION"), "El fallback debe incluir el tipo de solicitud");
    }

    @Test
    @DisplayName("resumirSolicitud debe lanzar ResourceNotFoundException cuando solicitud no existe")
    void testResumirSolicitud_SolicitudNotFound_ThrowsException() {
        // Arrange
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE),
                "Debe lanzar ResourceNotFoundException cuando la solicitud no existe"
        );
        verify(llmClient, never()).sendPrompt(anyString());
    }

    @Test
    @DisplayName("resumirSolicitud debe validar propiedad cuando rol es ESTUDIANTE")
    void testResumirSolicitud_EstudianteNoOwner_ThrowsException() {
        // Arrange
        UUID otroUsuarioId = UUID.randomUUID();
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudTest));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                iaService.resumirSolicitud(solicitudId, otroUsuarioId, RolUsuario.ESTUDIANTE),
                "Debe lanzar excepción si ESTUDIANTE no es propietario"
        );
    }

    @Test
    @DisplayName("resumirSolicitud debe permitir acceso sin validar propiedad para ADMINISTRATIVO")
    void testResumirSolicitud_Administrativo_NoOwnershipCheck() {
        // Arrange
        UUID otroUsuarioId = UUID.randomUUID();
        String resumenEsperado = "Resumen administrativo";
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudTest));
        when(llmClient.sendPrompt(anyString())).thenReturn(resumenEsperado);

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, otroUsuarioId, RolUsuario.ADMINISTRATIVO);

        // Assert
        assertNotNull(resumen, "Administrativo debe poder ver resumen sin validar propiedad");
        assertEquals(resumenEsperado, resumen);
    }

    // ============= TESTS DE SUGERENCIA DE PRIORIDAD =============

    @Test
    @DisplayName("sugerirPrioridad debe parsear respuesta JSON y retornar ALTA correctamente")
    void testSugerirPrioridad_ParseJSON_RetornaALTA() {
        // Arrange
        String respuestaJSON = "{\"prioridad\": \"ALTA\", \"justificacion\": \"Impacto alto detectado\"}";
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .fechaLimite(LocalDate.now().plusDays(5))
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaJSON);

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(resultado, "La respuesta no debe ser nula");
        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida(), "Debe parsear prioridad ALTA");
        assertEquals(75, resultado.getPuntajeTotal(), "Prioridad ALTA debe tener puntaje 75");
        assertNotNull(resultado.getRazones(), "Las razones no deben ser nulas");
        assertTrue(resultado.getRazones().size() > 0, "Debe haber al menos una razón");
    }

    @Test
    @DisplayName("sugerirPrioridad debe parsear MEDIA correctamente")
    void testSugerirPrioridad_ParseJSON_RetornaMedia() {
        // Arrange
        String respuestaJSON = "{\"prioridad\": \"MEDIA\", \"justificacion\": \"Prioridad media\"}";
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA)
                .impactoAcademico(ImpactoAcademico.MEDIO)
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaJSON);

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida());
        assertEquals(50, resultado.getPuntajeTotal(), "Prioridad MEDIA debe tener puntaje 50");
    }

    @Test
    @DisplayName("sugerirPrioridad debe retornar fallback cuando JSON es inválido")
    void testSugerirPrioridad_InvalidJSON_RetornaFallback() {
        // Arrange
        String respuestaInvalida = "Esta no es una respuesta JSON válida";
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaInvalida);

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(resultado, "Debe retornar fallback");
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida(), "Fallback es MEDIA");
        assertEquals(50, resultado.getPuntajeTotal());
    }

    @Test
    @DisplayName("sugerirPrioridad debe retornar fallback cuando LLMClient lanza excepción")
    void testSugerirPrioridad_LLMClientException_RetornaFallback() {
        // Arrange
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.CRITICO)
                .build();
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Gemini API unavailable"));

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(resultado, "Debe retornar un fallback");
        assertNotNull(resultado.getPrioridadSugerida(), "La prioridad del fallback no debe ser nula");
        assertNotNull(resultado.getRazones(), "Las razones del fallback no deben ser nulas");
    }

    // ============= TESTS DE CLASIFICACIÓN Y PRIORIDAD =============

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe parsear respuesta correctamente")
    void testSugerirClasificacionYPrioridad_ParseJSON_Correcto() {
        // Arrange
        String respuestaJSON = "{" +
                "\"tipoSolicitud\": \"HOMOLOGACION\", " +
                "\"prioridad\": \"ALTA\", " +
                "\"confianza\": 0.92, " +
                "\"justificacion\": \"Palabra clave homologación detectada\"" +
                "}";
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Necesito convalidar mis asignaturas de la universidad anterior")
                .canalOrigen(CanalOrigen.CORREO)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .fechaLimite(LocalDate.now().plusDays(10))
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaJSON);

        // Act
        SugerirClasificacionPrioridadResponseDTO resultado = 
                iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(resultado, "La respuesta no debe ser nula");
        assertEquals(TipoSolicitud.HOMOLOGACION, resultado.getTipoSolicitudSugerido());
        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida());
        assertEquals(0.92, resultado.getConfianza(), "Debe parsear confianza correctamente");
        assertFalse(resultado.isRequiereConfirmacionHumana(), "Confianza 0.92 no requiere confirmación");
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe indicar confirmación si confianza < 0.7")
    void testSugerirClasificacionYPrioridad_BajaConfianza_RequiereConfirmacion() {
        // Arrange
        String respuestaJSON = "{" +
                "\"tipoSolicitud\": \"CONSULTA_ACADEMICA\", " +
                "\"prioridad\": \"MEDIA\", " +
                "\"confianza\": 0.45, " +
                "\"justificacion\": \"Descripción ambigua\"" +
                "}";
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Tengo una pregunta sobre mi expediente")
                .canalOrigen(CanalOrigen.CORREO)
                .impactoAcademico(ImpactoAcademico.BAJO)
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaJSON);

        // Act
        SugerirClasificacionPrioridadResponseDTO resultado = 
                iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertTrue(resultado.isRequiereConfirmacionHumana(), 
                "Confianza 0.45 debe indicar que requiere confirmación");
        assertEquals(0.45, resultado.getConfianza());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar fallback si JSON inválido")
    void testSugerirClasificacionYPrioridad_InvalidJSON_RetornaFallback() {
        // Arrange
        String respuestaInvalida = "Esto no es JSON";
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Test description")
                .canalOrigen(CanalOrigen.PRESENCIAL)
                .impactoAcademico(ImpactoAcademico.BAJO)
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaInvalida);

        // Act
        SugerirClasificacionPrioridadResponseDTO resultado = 
                iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(resultado, "Debe retornar un fallback");
        assertEquals(TipoSolicitud.CONSULTA_ACADEMICA, resultado.getTipoSolicitudSugerido());
        assertEquals(Prioridad.MEDIA, resultado.getPrioridadSugerida());
        assertEquals(0.0, resultado.getConfianza());
        assertTrue(resultado.isRequiereConfirmacionHumana());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe manejar timeout de Gemini")
    void testSugerirClasificacionYPrioridad_GeminiTimeout_RetornaFallback() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Test")
                .canalOrigen(CanalOrigen.TELEFONICO)
                .impactoAcademico(ImpactoAcademico.MEDIO)
                .build();
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Request timeout after 10 seconds"));

        // Act
        SugerirClasificacionPrioridadResponseDTO resultado = 
                iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(resultado, "Debe retornar fallback incluso en timeout");
        assertNotNull(resultado.getTipoSolicitudSugerido(), "Tipo no debe ser nulo");
        assertNotNull(resultado.getPrioridadSugerida(), "Prioridad no debe ser nula");
    }

    // ============= TESTS DE DESACOPLAMIENTO =============

    @Test
    @DisplayName("IAServiceLLM debe depender de LLMClient y no de implementación específica")
    void testDesacoplamiento_DependenciaDeInterfaz() {
        // Arrange
        when(llmClient.sendPrompt(anyString()))
                .thenReturn("{\"prioridad\": \"ALTA\", \"justificacion\": \"Test\"}");

        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .build();

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(resultado, "El servicio está desacoplado de la implementación específica");
        verify(llmClient, times(1)).sendPrompt(anyString());
    }

    @Test
    @DisplayName("Múltiples llamadas a Gemini deben ser independientes")
    void testIndependencia_MultiplesLlamadas() {
        // Arrange
        when(llmClient.sendPrompt(anyString()))
                .thenReturn("{\"prioridad\": \"ALTA\", \"justificacion\": \"Test 1\"}")
                .thenReturn("{\"prioridad\": \"BAJA\", \"justificacion\": \"Test 2\"}");

        SugerirPrioridadRequestDTO dto1 = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .build();

        SugerirPrioridadRequestDTO dto2 = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA)
                .impactoAcademico(ImpactoAcademico.BAJO)
                .build();

        // Act
        SugerirPrioridadResponseDTO resultado1 = iaService.sugerirPrioridad(dto1);
        SugerirPrioridadResponseDTO resultado2 = iaService.sugerirPrioridad(dto2);

        // Assert
        assertEquals(Prioridad.ALTA, resultado1.getPrioridadSugerida());
        assertEquals(Prioridad.BAJA, resultado2.getPrioridadSugerida());
        verify(llmClient, times(2)).sendPrompt(anyString());
    }

    // ============= TESTS DE RESILIENCIA (RF-11) =============

    @Test
    @DisplayName("Sistema debe ser resiliente a fallos de Gemini (RF-11)")
    void testResiliencia_GeminiFalla_NoRompeElSistema() {
        // Arrange: Simular que Gemini falla
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Gemini API Error 429: Rate limit exceeded"));

        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.CRITICO)
                .build();

        // Act & Assert: El sistema debe continuar funcionando
        assertDoesNotThrow(() -> {
            SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);
            assertNotNull(resultado, "El sistema debe retornar un fallback");
        }, "El sistema NO debe lanzar excepción cuando Gemini falla");
    }

    @Test
    @DisplayName("Fallback debe ser consistente para múltiples errores")
    void testResiliencia_FallbackConsistente() {
        // Arrange
        when(llmClient.sendPrompt(anyString()))
                .thenThrow(new RuntimeException("Gemini error"));

        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .build();

        // Act: Llamar múltiples veces
        SugerirPrioridadResponseDTO resultado1 = iaService.sugerirPrioridad(dto);
        SugerirPrioridadResponseDTO resultado2 = iaService.sugerirPrioridad(dto);

        // Assert: Fallback debe ser consistente
        assertEquals(resultado1.getPrioridadSugerida(), resultado2.getPrioridadSugerida(),
                "El fallback debe ser consistente");
        assertEquals(Prioridad.MEDIA, resultado1.getPrioridadSugerida(),
                "El fallback siempre debe ser MEDIA");
    }

    @Test
    @DisplayName("Limpieza de markdown en respuesta JSON")
    void testParsing_JSONConMarkdown_ParseoCorrectoAlLimpiar() {
        // Arrange: Gemini a veces retorna JSON con ```json
        String respuestaConMarkdown = "```json\n{\"prioridad\": \"CRITICA\", \"justificacion\": \"Urgente\"}\n```";
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.CRITICO)
                .build();
        when(llmClient.sendPrompt(anyString())).thenReturn(respuestaConMarkdown);

        // Act
        SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

        // Assert
        assertEquals(Prioridad.CRITICA, resultado.getPrioridadSugerida(),
                "Debe limpiar markdown y parsear correctamente");
        assertEquals(100, resultado.getPuntajeTotal());
    }
}
