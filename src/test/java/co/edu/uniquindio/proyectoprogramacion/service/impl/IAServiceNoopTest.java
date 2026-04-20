package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IAServiceNoop Tests")
class IAServiceNoopTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    private IAServiceNoop iaService;
    private UUID solicitudId;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        iaService = new IAServiceNoop(solicitudRepository);
        solicitudId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
    }

    // ============= RESUMIR SOLICITUD TESTS =============

    @Test
    @DisplayName("resumirSolicitud debe retornar resumen cuando ESTUDIANTE es propietario de la solicitud")
    void testResumirSolicitud_EstudianteOwner_Success() {
        // Arrange
        Usuario solicitante = crearUsuario(usuarioId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE);

        // Assert
        assertNotNull(resumen);
        assertTrue(resumen.contains(solicitudId.toString()));
        assertTrue(resumen.contains("Resumen de solicitud"));
        assertTrue(resumen.contains("Solicitud académica procesada"));
    }

    @Test
    @DisplayName("resumirSolicitud debe lanzar excepción cuando ESTUDIANTE NO es propietario de la solicitud")
    void testResumirSolicitud_EstudianteNotOwner_ThrowsException() {
        // Arrange
        UUID otherUsuarioId = UUID.randomUUID();
        Usuario otherUser = crearUsuario(otherUsuarioId, "other@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Usuario solicitante = crearUsuario(usuarioId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            iaService.resumirSolicitud(solicitudId, otherUsuarioId, RolUsuario.ESTUDIANTE)
        );

        assertEquals("Solicitud no encontrada con ID: " + solicitudId, exception.getMessage());
    }

    @Test
    @DisplayName("resumirSolicitud debe permitir acceso a ADMINISTRATIVO sin validar propiedad")
    void testResumirSolicitud_Administrativo_NoOwnershipCheck() {
        // Arrange
        UUID administrativoId = UUID.randomUUID();
        UUID differentStudentId = UUID.randomUUID();
        Usuario estudiante = crearUsuario(differentStudentId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, estudiante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, administrativoId, RolUsuario.ADMINISTRATIVO);

        // Assert
        assertNotNull(resumen);
        assertTrue(resumen.contains("Resumen de solicitud"));
    }

    @Test
    @DisplayName("resumirSolicitud debe permitir acceso a COORDINADOR sin validar propiedad")
    void testResumirSolicitud_Coordinador_NoOwnershipCheck() {
        // Arrange
        UUID coordinadorId = UUID.randomUUID();
        UUID differentStudentId = UUID.randomUUID();
        Usuario estudiante = crearUsuario(differentStudentId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, estudiante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, coordinadorId, RolUsuario.COORDINADOR);

        // Assert
        assertNotNull(resumen);
        assertTrue(resumen.contains("Resumen de solicitud"));
    }

    @Test
    @DisplayName("resumirSolicitud debe permitir acceso a CONSULTOR sin validar propiedad")
    void testResumirSolicitud_Consultor_NoOwnershipCheck() {
        // Arrange
        UUID consultorId = UUID.randomUUID();
        UUID differentStudentId = UUID.randomUUID();
        Usuario estudiante = crearUsuario(differentStudentId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, estudiante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, consultorId, RolUsuario.CONSULTOR);

        // Assert
        assertNotNull(resumen);
        assertTrue(resumen.contains("Resumen de solicitud"));
    }

    @Test
    @DisplayName("resumirSolicitud debe lanzar ResourceNotFoundException cuando la solicitud no existe")
    void testResumirSolicitud_SolicitudNotFound_ThrowsException() {
        // Arrange
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE)
        );

        assertEquals("Solicitud no encontrada con ID: " + solicitudId, exception.getMessage());
    }

    @Test
    @DisplayName("resumirSolicitud debe incluir historial en el mensaje")
    void testResumirSolicitud_MustIncludeHistorialConsultMessage() {
        // Arrange
        Usuario solicitante = crearUsuario(usuarioId, "student@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        String resumen = iaService.resumirSolicitud(solicitudId, usuarioId, RolUsuario.ESTUDIANTE);

        // Assert
        assertTrue(resumen.contains("historial"));
    }

    // ============= SUGERIR PRIORIDAD TESTS =============

    @Test
    @DisplayName("sugerirPrioridad debe retornar SugerirPrioridadResponseDTO con prioridad MEDIA por defecto")
    void testSugerirPrioridad_ReturnsPrioridadMedia() {
        // Arrange
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();
        dto.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);

        // Act
        SugerirPrioridadResponseDTO response = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(Prioridad.MEDIA, response.getPrioridadSugerida());
    }

    @Test
    @DisplayName("sugerirPrioridad debe retornar puntajeTotal de 50")
    void testSugerirPrioridad_PuntajeTotalIs50() {
        // Arrange
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();

        // Act
        SugerirPrioridadResponseDTO response = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getPuntajeTotal());
    }

    @Test
    @DisplayName("sugerirPrioridad debe incluir razones de no disponibilidad de IA")
    void testSugerirPrioridad_MustIncludeReasons() {
        // Arrange
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();

        // Act
        SugerirPrioridadResponseDTO response = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(response.getRazones());
        assertTrue(response.getRazones().size() > 0);
        assertTrue(response.getRazones().stream()
            .anyMatch(r -> r.contains("IA no disponible") || r.contains("noop")));
    }

    @Test
    @DisplayName("sugerirPrioridad debe retornar respuesta consistente para múltiples llamadas")
    void testSugerirPrioridad_ConsistentResponse() {
        // Arrange
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();

        // Act
        SugerirPrioridadResponseDTO response1 = iaService.sugerirPrioridad(dto);
        SugerirPrioridadResponseDTO response2 = iaService.sugerirPrioridad(dto);

        // Assert
        assertEquals(response1.getPrioridadSugerida(), response2.getPrioridadSugerida());
        assertEquals(response1.getPuntajeTotal(), response2.getPuntajeTotal());
    }

    // ============= SUGERIR CLASIFICACION Y PRIORIDAD TESTS =============

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar tipo CONSULTA_ACADEMICA por defecto")
    void testSugerirClasificacionYPrioridad_ReturnsTipoConsultaAcademica() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(TipoSolicitud.CONSULTA_ACADEMICA, response.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar prioridad MEDIA por defecto")
    void testSugerirClasificacionYPrioridad_ReturnsPrioridadMedia() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(Prioridad.MEDIA, response.getPrioridadSugerida());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar confianza de 0.5")
    void testSugerirClasificacionYPrioridad_ConfianzaIs0_5() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(0.5, response.getConfianza());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar puntaje total 50")
    void testSugerirClasificacionYPrioridad_PuntajeTotalIs50() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getPuntajeTotal());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe indicar que requiere confirmación humana")
    void testSugerirClasificacionYPrioridad_RequiereConfirmacionHumana() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response);
        assertTrue(response.isRequiereConfirmacionHumana());
    }

    @Test
    @DisplayName("sugerirClasificacionYPrioridad debe retornar respuesta completa con todas las razones")
    void testSugerirClasificacionYPrioridad_CompleteResponse() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = new SugerirClasificacionPrioridadRequestDTO();

        // Act
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(response.getTipoSolicitudSugerido());
        assertNotNull(response.getPrioridadSugerida());
        assertTrue(response.getConfianza() >= 0 && response.getConfianza() <= 1);
        assertNotNull(response.getRazones());
        assertTrue(response.getRazones().size() > 0);
    }

    // ============= HELPER METHODS =============

    private Solicitud crearSolicitud(UUID id, Usuario solicitante) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(id);
        solicitud.setDescripcion("Solicitud de prueba");
        solicitud.setSolicitante(solicitante);
        solicitud.setFechaHoraRegistro(LocalDateTime.now());
        return solicitud;
    }

    private Usuario crearUsuario(UUID id, String username, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setIdentificacion("id_" + username);
        usuario.setNombres("Usuario");
        usuario.setApellidos("Test");
        usuario.setActivo(true);
        return usuario;
    }
}
